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
import time
import os
import pickle
import multiprocessing as mp
import sys
import json


def establish_benign():
    benign_host = 'NUC3Local'
    benign_port = 9091
    try:
        s_ = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s_.connect((benign_host, benign_port))
        return s_
    except:
        print("Error: Benign Establish")
        sys.exit(-1)


def establish_data():
    data_host = 'NUC1Local'
    data_port = 9090
    try:
        s_ = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s_.connect((data_host, data_port))
        return s_
    except:
        print("Error: Data Establish")
        sys.exit(-1)


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


def send_request(remote_host, remote_port, result_queue, request, sleep_time):
    time.sleep(sleep_time)
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((remote_host, remote_port))
        to_send = request+'\n'
        a = time.time()
        s.sendall(to_send.encode())
        response = s.recv(1024).decode().strip()
        b = time.time()
        s.close()
        result_queue.put({"request": request, "response": response, "runtime": b - a})
    except:
        result_queue.put("Sample Error")


def attack_requests(remote_host, remote_port, mal_requests, s_benign_,
                    benign_request, timeout, sample):
    result_queue = mp.Queue()
    print("\t\tStarting Attacker Send")
    process_list = [mp.Process(target=send_request,
                               args=(remote_host, remote_port, result_queue,
                                     mal_request["request"], 
                                     mal_request["sleep_time"]),
                               daemon=True) for mal_request in mal_requests]
    for p in process_list:
        p.start()
    
    # Launch benign request
    time.sleep(2)
    send_benign_command_1 = {"BENIGN_Command": "Send",
                             "REMOTE_HOST": remote_host,
                             "REMOTE_PORT": remote_port,
                             "BENIGN_Request": benign_request,
                             "Timeout": timeout,
                             "Sample": sample}
    send_cmd(s_benign_, json.dumps(send_benign_command_1))
    print("\t\tStarting Benign Send")
    
    # Wait benign request
    benign_msg_in_1 = receive_cmd(s_benign_)
    benign_data_in_1 = json.loads(benign_msg_in_1)
    assert(benign_data_in_1["BENIGN_Status"] == "Response In")
    print("\t\tBenign Response Received")
    
    for p in process_list:
        p.join()
    for p in process_list:
        p.terminate()
    results = {"attack_response": [result_queue.get() for mal_request in mal_requests],
               "benign_response": benign_data_in_1}
    return results


def run_experiment(remote_host, remote_port, samples):
    timeout = None
    # Establish data
    s_data_ = establish_data()
    # Establish benign
    s_benign_ = establish_benign()
    
    # {attack_response, benign_response, data_in}    
    runtime_data_list = []
    
    # Start sampling
    for sample in range(1, samples+1, 1):
        print("Sample:", sample, "of", samples)
        sys.stdout.flush()
        
        # Inform data host to start
        send_data_command_1 = {"DATA_Command": "Start", "REMOTE_PORT": remote_port, "Sample": sample}
        send_cmd(s_data_, json.dumps(send_data_command_1))
        data_msg_in_1 = receive_cmd(s_data_)
        data_in_1 = json.loads(data_msg_in_1)
        assert(data_in_1["DATA_Status"] == "Started")
        print("\tA", "Starting")
        print("\t\tStarting Data Collection")
        sys.stdout.flush()
        
        # Send attack requests
        mal_requests = [{"request": "99", "sleep_time": 0} for _ in range(10)]
        benign_request = "0"
        runtime_data = attack_requests(remote_host, remote_port, mal_requests,
                                       s_benign_, benign_request, timeout, sample)
        benign_response = runtime_data["benign_response"]
        
        # Stop data collection
        send_data_command_2 = {"DATA_Command": "Stop", "Sample": sample}
        send_cmd(s_data_, json.dumps(send_data_command_2))
        data_msg_in_2 = receive_cmd(s_data_)
        
        # Process data host response
        data_in_2 = json.loads(data_msg_in_2)
        assert(data_in_2["DATA_Status"] == "Stopped")
        
        # Assess data from data host
        if benign_response["BENIGN_Response"] == "NA" or data_in_2["Sample_Status"] == "Sample Error":
            runtime_data["data_in"] = timeout
        else:
            runtime_data["data_in"] = data_in_2["Data"]
        runtime_data_list.append(runtime_data["data_in"])
        print("\tB", "Stopping")
        print("\t\tProceeding to Next")
        sys.stdout.flush()
    
    # Inform data server complete
    send_data_command_3 = {"DATA_Command": "Complete"}
    send_cmd(s_data_, json.dumps(send_data_command_3))
    data_msg_in_3 = receive_cmd(s_data_)
    data_in_3 = json.loads(data_msg_in_3)
    assert(data_in_3["DATA_Status"] == "Complete")
    close_cmd(s_data_)
    
    # Inform benign server complete
    send_benign_command_2 = {"BENIGN_Command": "Complete"}
    send_cmd(s_benign_, json.dumps(send_benign_command_2))
    benign_msg_in_2 = receive_cmd(s_benign_)
    benign_data_in_2 = json.loads(benign_msg_in_2)
    assert(benign_data_in_2["BENIGN_Status"] == "Complete")
    close_cmd(s_benign_)
    
    print("Complete")
    sys.stdout.flush()
    return runtime_data_list    


def save_results(filename, results_data):
    os.chdir("Data")
    with open(filename+".p", "wb") as out_file:
        pickle.dump(results_data, out_file)
    os.chdir("..")


def main():
    remote_host = 'NUC2Local'
    remote_port = 8000
    data_filename = "Data"
        
    print("Starting Sampling Host:", remote_host, ":", remote_port)
    sys.stdout.flush()
    results = run_experiment(remote_host, remote_port, 2)
    print(results)
    sys.stdout.flush()
    save_results(data_filename, results)

if __name__ == "__main__":
    main()
