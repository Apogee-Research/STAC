#MIT License
#
#Copyright (c) 2017 Apogee Research
#
#Permission is hereby granted, free of charge, to any person obtaining a copy
#of this software and associated documentation files (the "Software"), to deal
#in the Software without restriction, including without limitation the rights
#to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#copies of the Software, and to permit persons to whom the Software is
#furnished to do so, subject to the following conditions:
#
#The above copyright notice and this permission notice shall be included in all
#copies or substantial portions of the Software.
#
#THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#SOFTWARE.


import socket
import sys
import time
import json


def benign_wait():
    data_host = 'NUC3Local'
    data_port = 9091
    s_ = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
            s_.bind((data_host, data_port))
    except:
            print('Socket Failed')
            s_.close()
            sys.exit()
    s_.listen(1)
    c_, address = s_.accept()
    return c_


def send_cmd(c_, message):
    to_send = message+"\r"
    c_.sendall(to_send.encode())


def receive_cmd(c_):
    msg_char = ""
    message = ""
    while msg_char != "\r":
        msg_char = c_.recv(1).decode()
        message += msg_char
    return message.strip()


def close_cmd(c_):
    c_.close()


def send_request(remote_host, remote_port, request, timeout):
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((remote_host, remote_port))
        to_send = request+'\n'
        a = time.time()
        s.sendall(to_send.encode())
        s.settimeout(timeout)
        response = s.recv(1024).decode().strip()
        b = time.time()
        s.close()
        return {"BENIGN_Response": response, "BENIGN_Runtime": b - a}
    except socket.timeout:
        return {"BENIGN_Response": "NA", "BENIGN_Runtime": timeout}
    except:
        print("Unexpected error:", sys.exc_info()[0])
        sys.stdout.flush()
        return "Sample Error"


def collect_data():
    c_ = benign_wait()
    while True:
        benign_msg_in_1 = receive_cmd(c_)
        command_in_1 = json.loads(benign_msg_in_1)

        # Check if done data collecting
        if command_in_1["BENIGN_Command"] == "Complete":
            print("C", "Complete")
            sys.stdout.flush()
            return_data_2 = {"BENIGN_Status": "Complete"}
            send_cmd(c_, json.dumps(return_data_2))
            break
        
        # Verify send command and send benign request
        assert(command_in_1["BENIGN_Command"] == "Send")
        print("Sample:", command_in_1["Sample"])
        print("\tA", "Sending")
        
        # Send and process data
        return_data_1 = send_request(command_in_1["REMOTE_HOST"], command_in_1["REMOTE_PORT"],
                                     command_in_1["BENIGN_Request"], command_in_1["Timeout"])
        print("\n\n\n", return_data_1, command_in_1["Timeout"], "\n\n\n")
        sys.stdout.flush()
        return_data_1["BENIGN_Status"] = "Response In"
        send_cmd(c_, json.dumps(return_data_1))
    close_cmd(c_)


def main():
    print("Starting Benign Collection")
    sys.stdout.flush()
    collect_data()

if __name__ == "__main__":
    main()
