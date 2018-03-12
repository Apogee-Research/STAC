from Crypto.PublicKey import RSA
from Crypto.Util.number import inverse
import rsa_gen

import comms_connection

E = 65537L

class CommsClient(object):

    def __init__(self, name, rsa_prime1_file=None, rsa_prime2_file=None):
        self.name = name
        if rsa_prime1_file is None:
            self.rsa = create_new_rsa()
        else:
            self.rsa = generate_rsa(rsa_prime1_file, rsa_prime2_file)

    def connect(self, host, port, handler, our_host=None, our_port=None):
        if our_host is None or our_port is None:
            self.connection = comms_connection.Connection(host, port, handler, self.name, self.rsa)
        else:
            self.connection = comms_connection.Connection(host, port, handler, self.name, self.rsa, our_host, our_port)
        return self.connection

def create_new_rsa():
    prime1, prime2 = rsa_gen.generate_primes()
    return generate_rsa_from_primes(prime1, prime2)

def generate_rsa(prime1_file, prime2_file):
    """
    :param prime1: file containing the first rsa prime
    :param prime2: file containing the second rsa prime
    :return: the rsa instance and the public modulus
    """
    prime1, prime2 = read_primes(prime1_file, prime2_file)
    return generate_rsa_from_primes(prime1, prime2)

def generate_rsa_from_primes(prime1, prime2):
    public_mod = prime1*prime2
    d = inverse(E, (prime1 - 1)*(prime2 - 1))
    rsa_tuple = (public_mod, E, d, prime1, prime2)
    rsa_impl = RSA.RSAImplementation(use_fast_math=False)
    return rsa_impl.construct(rsa_tuple)


def read_primes(p_file, q_file=None):
    """
    Reads primes p and q in from file(s) and converts them to python integers.
    """
    if q_file is None:
        # both primes are in the same file on different lines
        with open(p_file) as f:
            pstr = f.readline()
            qstr = f.readline()
            return (int(pstr), int(qstr))


    with open(p_file) as f:
        pstr = f.read()
    p = int(pstr)
    with open(q_file) as f:
        qstr = f.read()
    q = int(qstr)
    return (p,q)
