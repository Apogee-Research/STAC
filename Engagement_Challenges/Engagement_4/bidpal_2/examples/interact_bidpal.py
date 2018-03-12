import subprocess
import time
import tempfile

class BidPalUser:
    def __init__(self, bin_dir, port, user_id):
        self.debugging = False
        self.user_id = user_id
        self.port = port
        file = tempfile.NamedTemporaryFile(suffix=user_id, delete=not self.debugging)
        self.fw = file # output file for bidpal process
        self.fr = open(file.name, "r") # reader for output of bidpal process
        self.process = subprocess.Popen(bin_dir + "/bidpal " + str(port) + " " + user_id,
                                stdout=self.fw, stdin=subprocess.PIPE,
                                stderr=self.fw, shell=True)
        self.wait_for_output("/quit") # the program prints a list of commands when it starts

    def connect(self, host, port):
        self.process.stdin.write("/connect " + host + " " + port +"\n")
        self.wait_for_output("Connected")
        self.fr.readline().strip() # read output (just so we're processing output as it comes)
        self.fr.readline().strip()

    def start_auction(self, description, id=None):
        if id==None:
            self.process.stdin.write("/start " + description +"\n")
        else:
            self.process.stdin.write("/startid " + id + " " + description + "\n")

    def end_auction(self, auction_id):
        self.process.stdin.write("/close " + auction_id + "\n")

    def list_auctions(self):
        self.process.stdin.write("/listauctions\n")
        self.print_output_between("Auctions:", "-----")

    def get_auction_status(self, auction_id):
        self.process.stdin.write("/status " + auction_id + "\n")
        self.print_output_between("Auction status:", "------")

    def get_bidders(self, auction_id):
        self.process.stdin.write("/listbidders " + auction_id + "\n")
        self.print_output_between("Bidder status for auction", "-----")

    def announce_winner(self, auction_id, user, bid):
        self.process.stdin.write("/winner " + auction_id + " " + user + " " + str(bid) + "\n")

    # make a bid and don't wait for response
    def bid(self, item_id, bid):
         self.process.stdin.write("/bid " + item_id + " " + str(bid) + "\n")

    # make a bid and wait for one or more responses
    def bid_and_wait_for_response(self, item_id, bid, num_responses=1):
        #make a bid, causing other bidders we're connected to to respond
        self.bid(item_id, bid)
        self.log("waiting for " + str(num_responses) + " responses")
        for i in range(num_responses):
            recLine = self.wait_for_output("received a bid comparison") # response from another bidder -- need to wait until this is done
            self.log("received " + str(i + 1) + "th response")
        print recLine

    def wait_for_compare_results(self):
        return self.wait_for_output("received a bid comparison")

    def quit(self):
        self.process.stdin.write("/quit\n")
        self.wait_for_output("quit!")
        self.fr.close()
        self.fw.close()

    def print_output_between(self, start_output, end_output):
        last_line="" # in case the output we're looking for gets split between lines, remember prev line
        should_print = False
        while self.process_is_running():
            output = self.fr.readline()

            if (output!='' and output!="\n"):
                '''if not output.endswith("\n"):
                    last_line = output.strip()
                    continue'''
                res = output.strip()
                if should_print:
                    print res
                    if end_output in res or end_output in last_line + res:
                        return
                else:
                    if start_output in res:
                        print res
                        should_print = True
                    elif start_output in last_line + res:
                        print last_line+res
                        should_print = True
                last_line = res # in case output got split, hold onto this
        raise Exception("process closed unexpectedly")

    def wait_for_output(self, exp_output=None):
        last_line="" # in case the output we're looking for gets split between lines, remember prev line
        self.log("waiting for " + exp_output)
        while self.process_is_running():
            time.sleep(0.001)
            output = self.fr.readline()
            if len(output) > 0:
                self.log("read: " + output)

            if (output!='' and output!="\n"):
                res = output.strip()
                if exp_output==None or exp_output in res:
                    return res
                elif exp_output in last_line+res:
                    return last_line+res
                else:
                    last_line = res # in case output got split, hold onto this
        raise Exception("process closed unexpectedly")

    def wait_for_outputs(self, exp_outputs):
        last_line="" # in case the output we're looking for gets split between lines, remember prev line
        collected_output=""
        self.log("waiting for " + ",".join(exp_outputs))
        while len(exp_outputs)>0 and self.process_is_running():
            time.sleep(0.001)
            output = self.fr.readline()

            if (output!='' and output!="\n"):
                res = output.strip()
                for out in exp_outputs:
                    if out in res:
                       collected_output+=out + "\n"
                       exp_outputs.remove(out)
                       self.log("waiting for " + ",".join(exp_outputs))
                       continue
                for out in exp_outputs:
                   if out in last_line+res:
                        collected_output+=last_line+res
                        exp_outputs.remove(out)
                        self.log("waiting for " + ",".join(exp_outputs))
                        continue
                else:
                    last_line = res # in case output got split, hold onto this

        if not self.process_is_running():
            raise Exception("process closed unexpectedly")

        return collected_output

    def process_is_running(self):
        return self.process.poll() == None

    def log(self, msg):
        if self.debugging:
            print self.user_id + ":", msg

    def get_user_id(self): return self.user_id