from comms_connection import Connection
import comms_pb2
import google.protobuf.text_format as text_format
from Crypto.Util.number import long_to_bytes, bytes_to_long

class MaliciousConnection(Connection):

    #TODO:We may not need this, but I'm keeping it here because I have a feeling we will want some other data in the malicious connection later
    def __init__(self, host, port, handler, name, rsa):
        super(MaliciousConnection, self).__init__(host, port, handler, name, rsa)

    def setup_connection(self, our_host=None, our_port=None):
        """
        This function sets up the portions of the connection before the RSA part of the exchange
        so that we can carry out the attack on that part of the exchange.  It is effectively the
        beginning segment of setup_connection.
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
        self.create_and_send_identity(serverDH_key)

        # read in the server's identity, decrypt it, and return their id, their rsa public key, and their rsa test
        self.server_id, server_rsa_pk, server_rsa_test = self.handle_server_identity()
        self.server_rsa_pk_long = bytes_to_long(server_rsa_pk.modulus)
        self.create_and_send_response(server_rsa_test)

    def create_malicious_challenge(self, u, Rinv, public_mod):
        """
        Sends a message that the server should think is the test resposne, when in actuallity we are just timing
        how long it takes them to decrypt the message and tell us that we are wrong.
        :param u: The value to decrypt based on the interactive attack
        :param Rinv: the inverse of the Montgomery factor R the RSA ring
        :param public_mod: The public modulus for the server's public RSA key
        """

        client_response = comms_pb2.ClientResponseToFailure()
        rsa_result = client_response.rsaResults
        rsa_result.results = long_to_bytes(1890L)

        time_value = (u * Rinv) % public_mod

        rsa_test = client_response.rsaTest
        rsa_test.test = long_to_bytes(time_value)

        return client_response.SerializeToString()

    def create_and_send_malicious_challenge(self, u, Rinv, public_mod):
        """
        Creates the malicious challenge, whose decryption we must time, and sends it to the server
        """
        client_malicious_challenge = self.create_malicious_challenge(u, Rinv, public_mod)
        encrypted_malicious_challenge = self.encrypt_msg(client_malicious_challenge)
        self.pack_and_send_message(encrypted_malicious_challenge)


