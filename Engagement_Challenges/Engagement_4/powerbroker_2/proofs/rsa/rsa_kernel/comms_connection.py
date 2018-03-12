import socket
import comms_pb2
import comms_client
import struct
from threading import Thread
from Crypto.Hash import SHA512, HMAC, SHA256
from Crypto.Cipher import AES
from Crypto.Random import random
from Crypto.PublicKey import RSA
from Crypto.Util import Counter
from Crypto.Util.number import inverse, bytes_to_long, long_to_bytes, getRandomInteger
from math import floor

modp1536 = 0xFFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF


class Connection(object):

    def __init__(self, host, port, handler, name, rsa, our_host=None, our_port=None):
        self.handler = handler
        self.name = name
        self.rsa = rsa
        self.keysize = 256
        self.keysize_bytes = self.keysize/8
        self.iv_size_bits = 128
        self.iv_size_bytes = self.iv_size_bits/8 + (0 if self.iv_size_bits & 0x7 == 0 else 1)

        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        if not (our_host is None or our_port is None):
            self.client_socket.bind((our_host, our_port))

        # connect to the server
        self.client_socket.connect((host, port))

        # create ffdh keys
        self.privateKey, self.publicKey = self.generate_ffdh()
        self.setup_connection(our_host, our_port)


    def setup_connection(self, our_host=None, our_port=None):
        """
        Creates and sends the client's setup messages.
        Receives the server's setup messages.
        :return: the socket
        """
        # make a client diffie-hellman public key message
        clientDH = comms_pb2.DHPublicKey()
        clientDH.key = long_to_bytes(self.publicKey)

        # code to send and receive messages found here and modified:
        # http://eli.thegreenplace.net/2011/08/02/length-prefix-framing-for-protocol-buffers
        # here the developer states this code is in the public domain:
        # http://eli.thegreenplace.net/pages/code

        # send our public key to the server
        self.pack_and_send_message(clientDH.SerializeToString())

        # read in the server's public key and set the diffie-hellman master secret
        serverDH_key = self.handle_server_dh_key()

        # send our identity
        self.create_and_send_identity(serverDH_key, our_host, our_port)

        # read in the server's identity, decrypt it, and return their id, their rsa public key, and their rsa test
        self.server_id, server_rsa_pk, server_rsa_test = self.handle_server_identity()


        # we need to take their test and construct our own
        self.create_and_send_response(server_rsa_test)

        # read in the server's response and decrypt it
        our_test_results = self.handle_server_response()

        # send them their results
        # TODO: should we actually check their answer?
        # for now we just send them back the results they sent us
        # encrypt their results
        self.send_rsa_results(our_test_results)

        self.start_recv_thread()

        return self.client_socket


    def close(self):
        """
        The handler will call this when they want to close the connection
        """
        self.keep_going = False
        self.client_socket.shutdown(socket.SHUT_RDWR)
        self.recv_thread.join()

    def is_connected(self):
        """
        :return: true if client_socket is still connected
        """


    def write(self, message):
        """
        The handler willl call this when they want to send a message
        :param message: to send
        """
        encrypted_text = self.encrypt_msg(message)
        self.pack_and_send_message(encrypted_text)

    def start_recv_thread(self):
        self.keep_going = True
        self.recv_thread = Thread(target=self.do_reads)
        self.recv_thread.start()



    def do_reads(self):
        while self.keep_going:
            data = self.read_in_message()
            if data is None:
                # If this is the case, it means the connection was closed or dropped
                return
            decrypted_msg = self.decrypt_msg(data)
            self.handler(self, decrypted_msg)

    # function from
    # http://eli.thegreenplace.net/2011/08/02/length-prefix-framing-for-protocol-buffers
    def socket_read(self, n):
        """
        Reads in data from the server
        :param n:
        :return: data
        """
        buf = ''
        try:
            while n > 0:
                data = self.client_socket.recv(n)
                if data == '':
                    raise RuntimeError('Unexpected connection close')
                buf += data
                n -= len(data)
        except socket.error:
            raise RuntimeError('Unexpected connection close')
        return buf

    def pad(self, message, length=192):
        ret = ''
        for i in xrange(length - len(message)):
            ret += chr(0)
        ret += message
        return ret

    def read_in_message(self):
        """
        Reads in a comms message.
        Some of this code is from
        http://eli.thegreenplace.net/2011/08/02/length-prefix-framing-for-protocol-buffers
        :return: message read in, usually IV + HMAC + CIPHERTEXT
        """
        len_buf = self.socket_read(4)
        message_len = struct.unpack('>L', len_buf)[0]
        message_buf = self.socket_read(message_len)
        return message_buf

    def create_cipher_hmac(self, iv):
        """
        Creates a new cipher and hmac given the diffie-hellman master secret, which the client
        and server both have, and the initialization vector.
        The initialization vector should be different every time this function is called.
        :param master_secret:
        :param iv:
        :return: cipher and hmac
        """
        # hash the master secret
        hash = SHA512.new()
        hash.update(self.pad(self.master_secret))
        hashed_master_secret = hash.digest()

        # create an AES symmetric cipher using the first half of the hashed master secret
        session_key_bytestring = hashed_master_secret[:self.keysize_bytes]
        ctr = Counter.new(self.iv_size_bits, initial_value=iv)
        cipher = AES.new(session_key_bytestring, AES.MODE_CTR, counter=ctr)

        # create an hmac using the second half of the hashed master secret
        hmac_key_bytestring = hashed_master_secret[self.keysize_bytes:]
        hmac = HMAC.new(hmac_key_bytestring, digestmod=SHA256)
        return cipher, hmac

    def encrypt_msg(self, msg):
        """
        Encrypts a message that is going to be sent to the server
        :param msg: to be encrypted
        :return: the encrypted message, its initialization vector, and its hmac
        """
        # create the iv that will be used to create the cipher
        iv = random.getrandbits(self.iv_size_bits)
        cipher, hmac = self.create_cipher_hmac(iv)
        encrypted_msg = cipher.encrypt(msg)
        hmac.update(encrypted_msg)
        msg_hmac = hmac.digest()
        iv_bytes = long_to_bytes(iv, self.iv_size_bytes)

        return iv_bytes + msg_hmac + encrypted_msg

    def decrypt_msg(self, msg):
        """
        Decrypts the given message using the shared diffie-hellman secret and the
        initialization vector sent with the message.
        :param msg: to be decrypted, should be IV + HMAC + CIPHERTEXT
        :return: the decrypted message
        """

        iv = msg[:self.iv_size_bytes]
        long_iv = bytes_to_long(iv)
        cipher, hmac = self.create_cipher_hmac(long_iv)
        # separate the message from the provided mac
        hmac_end = self.iv_size_bytes + self.keysize_bytes
        cipher_txt = msg[hmac_end:]
        provided_hmac = msg[self.iv_size_bytes:hmac_end]
        hmac.update(cipher_txt)
        computed_mac = hmac.digest()
        if provided_hmac != computed_mac:
            raise Exception("Computed and provided mac differ!" + "\n" + str(hex(bytes_to_long(provided_hmac))) \
                            + "\n" + str(hex(bytes_to_long(computed_mac))))
        # if the computed mac and the provided mac are the same
        # return the decrypted cipher text
        return cipher.decrypt(cipher_txt)

    def set_master_secret(self, serverDH):
        """
        Given the server's comms diffie-hellman message, get the server's public
        diffie-hellman key and compute the shared master secret.
        :param serverDH: comms_pb2.DHPublicKey
        :return: the diffie-hellman master secret
        """
        server_dhkey = bytes_to_long(serverDH.key)

        # get the diffie-hellman shared master secret
        master_secret = pow(server_dhkey, self.privateKey, modp1536)

        # convert the master secret to a byte string and hash it
        self.master_secret = long_to_bytes(master_secret)

    def get_server_identity_and_test(self, setup_msg):
        """
        Parses the server's comms ServerSetup message. It returns the server's name,
        their rsa public key, and their rsa test number.
        We are not using their rsa public key right now because we do not care if they
        pass our rsa test.
        :param setup_msg:
        :return: the server's name, their rsa public key, and their rsa test
        """
        server_sign_msg = comms_pb2.SignedMessage()
        server_sign_msg.ParseFromString(setup_msg)
        server_comms_msg = comms_pb2.CommsMsg()
        server_comms_msg.ParseFromString(server_sign_msg.data)
        server_setup = server_comms_msg.serverSetup
        server_name = server_setup.identity.id
        server_rsa_pk = server_setup.identity.publicKey
        server_rsa_test = server_setup.rsaTest.test
        return server_name, server_rsa_pk, server_rsa_test

    def get_server_results(self, server_response_msg):
        """
        Parses the server's comms ServerResponse message. It returns the server's rsa results
        (i.e. whether we passed the server's rsa test)
        :param server_response_msg:
        :return: rsa test results
        """
        server_response = comms_pb2.ServerResponse()
        server_response.ParseFromString(server_response_msg)
        return server_response.rsaResults.SerializeToString()

    def create_client_identity(self, server_dhkey, our_host=None, our_port=None):
        """
        Creates the client's comms SignedMessage, which contains the ClientSetup message.
        This message will include the client's identity, their rsa public key, and the server's
        public diffie-hellman key
        :param server_dhkey: the server's diffie-hellman public key
        :return: the signed message
        """
        client_signedmsg = comms_pb2.SignedMessage()
        client_commsmsg = comms_pb2.CommsMsg()
        client_commsmsg.type = comms_pb2._COMMSMSG_TYPE.values_by_name['CLIENT_SETUP'].number
        client_id = client_commsmsg.clientSetup

        server_publickey = client_id.key
        server_publickey.key = server_dhkey

        client_identity = client_id.identity
        client_identity.id = self.name
        client_publickey = client_identity.publicKey
        client_publickey.e = long_to_bytes(self.rsa.e)
        client_publickey.modulus = long_to_bytes(self.rsa.n)
        client_address = client_identity.callbackAddress
        if not (our_host is None or our_port is None):
            client_address.host = our_host
            client_address.port = our_port

        client_msg_string = client_commsmsg.SerializeToString()

        client_signedmsg.data = client_msg_string
        client_signedmsg.signedHash = self.sign_message(client_msg_string)

        return client_signedmsg.SerializeToString()

    def sign_message(self, message):
        """
        :param message: to be signed
        :return: the signed hash of the message
        """
        hash = SHA256.new()

        # hash the message before you sign it
        hash.update(message)
        hashed_message = hash.digest()
        long_hashed_msg = bytes_to_long(hashed_message)
        signed_msg = self.rsa.key._decrypt(long_hashed_msg)

        return long_to_bytes(signed_msg)


    def create_client_response_msg(self, rsa_test):
        """
        Creates the client's comms ClientResponse message, which includes the client's rsa test
        and the client's response to the server's rsa test.
        We should probably actually encrypt the client's rsa test with the server's public key,
        but it is unnecessary for now.
        :param rsa_test: the server's rsa test
        :return: the client's response message
        """
        # construct client response
        client_response = comms_pb2.ClientResponse()

        # construct rsa response for client response
        rsa_response = client_response.rsaResponse
        decrypted_test = self.rsa.key._decrypt(bytes_to_long(rsa_test))
        rsa_response.response = long_to_bytes(decrypted_test)

        # construct rsa results for client response
        # TODO: maybe we should actually encrypt this
        # using the server's public key, but for now
        # this will be fine
        random_int = getRandomInteger(self.keysize)
        rsa_test = client_response.rsaTest
        rsa_test.test = long_to_bytes(random_int)
        return client_response.SerializeToString()


    def pack_and_send_message(self, message):
        packed = struct.pack('>L', len(message))
        self.client_socket.sendall(packed + message)


    def create_and_send_identity(self, server_dhkey, our_host=None, our_port=None):
        """
        Creates the client's identity message and sends it to the server
        :param server_dhkey: the server's diffie-hellman key, which will be included in the
                             signed client message
        """

        # create a client setup message that is serialized to a string
        client_id_msg = self.create_client_identity(server_dhkey, our_host, our_port)

        # encrypt the message and add the hmac
        encrypted_client_id_msg = self.encrypt_msg(client_id_msg)

        # send our identity to the server
        self.pack_and_send_message(encrypted_client_id_msg)

    def create_and_send_response(self, server_rsa_test):
        """
        Creates the client's response message and sends it to the server
        :param server_rsa_test: the server's rsa test
        """
        client_response_msg = self.create_client_response_msg(server_rsa_test)

        # encrypt this message
        encrypted_response_msg = self.encrypt_msg(client_response_msg)

        # send the response
        self.pack_and_send_message(encrypted_response_msg)

    def send_rsa_results(self, results):
        """
        Sends the server their rsa results. Right now, we are sending them the
        same results they sent us
        :param results:
        """
        encrypted_results = self.encrypt_msg(results)

        # send the results
        self.pack_and_send_message(encrypted_results)

    def handle_server_dh_key(self):
        """
        Read in the server's diffie-hellman key and compute the shared master secret
        :return: the diffie-hellman master secret
        """
        message = self.read_in_message()
        serverDH = comms_pb2.DHPublicKey()
        serverDH.ParseFromString(message)

        # get the server's diffie-hellman key and generate the master secret
        self.set_master_secret(serverDH)
        return serverDH.key


    def handle_server_identity(self):
        """
        Read in the server's identity and get their id, their rsa public key, and their rsa test
        :return: the server's id, rsa key, and rsa test
        """
        message = self.read_in_message()
        decrypted_msg = self.decrypt_msg(message)

        # parse this message to get the server's rsa public key and their rsa test
        return self.get_server_identity_and_test(decrypted_msg)

    def handle_server_response(self):
        """
        Read in the server's response and return the results of their rsa test
        :return: their rsa test response (i.e. did the client pass the server's test)
        """
        message = self.read_in_message()
        decrypted_msg = self.decrypt_msg(message)

        # parse this message to get the server's results and their response
        # we don't really care whether or not we passed, but we should have
        return self.get_server_results(decrypted_msg)



    def generate_ffdh(self):
        """
        Generates an ffdh public, private key pair.
        :return: ffdh private key, ffdh public key
        """
        private_key = random.getrandbits(64)
        generator = 2
        public_key = pow(generator, private_key, modp1536)
        return private_key, public_key

    def set_host_port(self, host, port):
        self.client_socket.bind((host, port))
