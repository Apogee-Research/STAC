# MIT License
#
# Copyright (c) 2017 Apogee Research
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.


import socket
import sys
import subprocess
import json
import time
import os


def benign_wait():
    data_host = 'clientNuc'
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
    to_send = message + "\r"
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


def send_request(remote_host, remote_port, request):
    try:
        ben_client_out = open("ben_client_out.txt", "w")
        ben_client_err = open("ben_client_err.txt", "w")
        subprocess.call(["java", "-cp", "Category16.jar", "Category16_vulnerable_client", remote_host, request],
                        stdout=ben_client_out, stderr=ben_client_err)
        time.sleep(0.1)

        ben_client_out.close()
        ben_client_err.close()
        ben_client_out = open("ben_client_out.txt", "r")
        client_output = ben_client_out.readline()
        ben_client_out.close()

        # Remove Files
        os.remove("ben_client_out.txt")
        os.remove("ben_client_err.txt")

        return {"BENIGN_Response": client_output}
    except:
        print("Unexpected error:", sys.exc_info()[0])
        sys.stdout.flush()
        return {"BENIGN_Response": "NA"}


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
        assert (command_in_1["BENIGN_Command"] == "Send")
        print("Sample:", command_in_1["Sample"])
        print("\tA", "Sending")

        # Send and process data
        return_data_1 = send_request(command_in_1["REMOTE_HOST"], command_in_1["REMOTE_PORT"],
                                     command_in_1["BENIGN_Request"])
        print("\n\n\n", return_data_1, "\n\n\n")
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
