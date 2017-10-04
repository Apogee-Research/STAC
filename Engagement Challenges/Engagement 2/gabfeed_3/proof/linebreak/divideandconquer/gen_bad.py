import sys

def go(word_width, num_words):
    for i in xrange(num_words):
        print 'a' * word_width,

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print "usage: gen_bad.py <word width> <num words>"
        sys.exit(-1)
    go(int(sys.argv[1]), int(sys.argv[2]))