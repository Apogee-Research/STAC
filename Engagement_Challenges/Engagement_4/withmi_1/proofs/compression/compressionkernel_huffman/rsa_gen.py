from Crypto.PublicKey import RSA
import argparse

def generate_primes(keysize=1024):
    key = RSA.generate(keysize)
    return (key.p, key.q)

def write_prime_to_file(prime, filename):
    with open(filename, 'w') as f:
        f.write(str(prime))
    return

def write_both_primes(prime1, prime2, filename):
    with open (filename, 'w') as f:
        f.write(str(prime1) + '\n' + str(prime2) + '\n')
    return

def write_modulus(prime1, prime2, filename):
    with open(filename, 'w') as f:
        f.write(str(prime1 * prime2) + '\n')

def main():
    parser = argparse.ArgumentParser(description='Generate RSA primes')
    parser.add_argument('key_size', type=int, help='Size of the RSA public-key modulus')
    parser.add_argument('modulus_file', help='Destination file to write the RSA public key modulus')
    parser.add_argument('prime_files', nargs='+', help='Destination files to write the RSA secret primes.  Either one or two file names should be provided.')
    args = parser.parse_args()
    # this will produce two primes half the size of key_size
    (p, q) = generate_primes(args.key_size)
    if len(args.prime_files) == 2:
        write_prime_to_file(p, args.prime_files[0])
        write_prime_to_file(q, args.prime_files[1])
    elif len(args.prime_files) == 1:
        write_both_primes(p, q, args.prime_files[0])
    else:
        print "Error.  Provide only 1 or 2 filenames to store the private key primes"
    write_modulus(p,q,args.modulus_file)
    return

if __name__ == '__main__':
    main()
    
