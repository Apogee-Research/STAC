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
import os
import subprocess
import time
import sys
import json
import scapy.all as scapy


def data_wait():
    data_host = 'masterNuc'
    data_port = 9090
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


def start_watch(port):
    tcp_out = open("tcp_out.txt", "w")
    tcp_err = open("tcp_err.txt", "w")
    tcpdump = subprocess.Popen(["tcpdump", "-i", "enp0s20u4", "dst", "serverNuc", "-w", "data.pcap"],
                               stdout=tcp_out, stderr=tcp_err)
    time.sleep(0.5)
    return tcpdump, tcp_out, tcp_err


def stop_watch(tcpdump, tcp_out, tcp_err):
    tcpdump.terminate()
    tcp_out.close()
    tcp_err.close()
    time.sleep(0.2)


def process_data():
    pcap_file = scapy.rdpcap("data.pcap")

    # Parse pcap
    # Parse pcap
    started = False
    stopped = False
    extracted_bits = False
    extracted_size = False
    times = []
    time_diffs = []
    extracted_string = ""
    extracted_bit_string = ""
    extracted_size_string = ""
    for i in range(len(pcap_file.res)):
        ether_packet = pcap_file.res[i]
        ether_time = ether_packet.time
        if i != 0:
            # time in ms
            time_diffs.append((ether_time - times[-1]) * 1000)

            # transmit start 100 ms expected
            if time_diffs[-1] > 100 and not started:
                started = True
                extracted_string = ""
            # transmit stop 100 ms expected
            elif time_diffs[-1] > 100 and started and not stopped:
                stopped = True
                if not extracted_bits and not extracted_size:
                    extracted_bit_string = extracted_string
                    extracted_bits = True
                    started = False
                    stopped = False
                elif not extracted_size:
                    extracted_size_string = extracted_string
                    extracted_size = True
            # side channel: data = 1 60 ms expected
            #               data = 0 30 ms expected
            elif started and not stopped:
                # data = 1 60 ms expected
                if time_diffs[-1] > 60:
                    extracted_string += "1"
                # data = 0 30 ms expected
                else:
                    extracted_string += "0"
        times.append(ether_time)

    # Remove Files
    os.remove("tcp_out.txt")
    os.remove("tcp_err.txt")
    os.remove("data.pcap")

    if extracted_bit_string != "" and extracted_size_string != "":
        return {"Bit String": extracted_bit_string,
                "Size String": extracted_size_string}
    return "Error"


def collect_data():
    c_ = data_wait()
    while True:
        data_msg_in_1 = receive_cmd(c_)
        command_in_1 = json.loads(data_msg_in_1)

        # Check if done data collecting
        if command_in_1["DATA_Command"] == "Complete":
            print("C", "Complete")
            sys.stdout.flush()
            return_data_3 = {"DATA_Status": "Complete"}
            send_cmd(c_, json.dumps(return_data_3))
            break

        # Verify start command and start tcpdump
        assert (command_in_1["DATA_Command"] == "Start")
        sample = command_in_1["Sample"]
        print("Sample:", sample)
        print("\tA", "Starting")
        tcpdump, tcp_out, tcp_err = start_watch(command_in_1["REMOTE_PORT"])
        time.sleep(0.2)
        print("\t\tStarted tcpdump")
        sys.stdout.flush()

        # Inform client tcpdump started
        return_data_1 = {"DATA_Status": "Started", "Sample": sample}
        send_cmd(c_, json.dumps(return_data_1))

        # Listen on tcpdump
        print("\t\tListening")
        sys.stdout.flush()

        # Verify stop command and stop listening
        data_msg_in_2 = receive_cmd(c_)
        command_in_2 = json.loads(data_msg_in_2)
        assert (command_in_2["DATA_Command"] == "Stop")
        assert (command_in_2["Sample"] == sample)
        print("\tB", "Stopping")
        stop_watch(tcpdump, tcp_out, tcp_err)

        # Process data
        trip_data = process_data()
        if trip_data != "Error":
            return_data_2 = {"Sample_Status": "OK", "Data": trip_data}
        else:
            print("\t\tError Sample:", sample)
            sys.stdout.flush()
            return_data_2 = {"Sample_Status": "Sample Error"}

        # Inform client tcpdump stopped and processing complete
        # Send client collected data
        print("\t\tStopped tcpdump and processing")
        sys.stdout.flush()
        return_data_2["DATA_Status"] = "Stopped"
        return_data_2["Sample"] = sample
        send_cmd(c_, json.dumps(return_data_2))
    close_cmd(c_)


def main():
    print("Starting Data Collection")
    sys.stdout.flush()
    collect_data()


if __name__ == "__main__":
    main()
