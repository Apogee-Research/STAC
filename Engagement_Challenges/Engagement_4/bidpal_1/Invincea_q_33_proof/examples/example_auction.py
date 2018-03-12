import time
from interact_bidpal import BidPalUser
bin_dir = "../challenge_program/bin"

def main():
    user0 = BidPalUser(bin_dir, "8000", "seller")
    user1 = BidPalUser(bin_dir, "8001", "user1")
    user2 = BidPalUser(bin_dir, "8002", "user2")
    user3 = BidPalUser(bin_dir, "8003", "user3")
    
    time.sleep(1)
    user0.connect("127.0.0.1", "8002")
    user0.connect("127.0.0.1", "8001")
    user0.connect("127.0.0.1", "8003")
    user1.connect("127.0.0.1", "8002")
    user3.connect("127.0.0.1", "8001")
    user3.connect("127.0.0.1", "8002")
    
    user0.start_auction("Applied Cryptography book, new condition", "auction1")

    # give users a chance to receive and process auction start message
    user1.wait_for_output("received auction start announcement")
    user2.wait_for_output("received auction start announcement")
    user3.wait_for_output("received auction start announcement") 
    time.sleep(1) # give users time to process the start announcement
    # two users bid
    user1.bid("auction1", "10") 
    user2.wait_for_output("received a bid commitment")
    user2.bid("auction1", "11")
    
    # wait for comparisons to happen
    print user1.wait_for_compare_results() # user1's bid compared to user2's
    print user2.wait_for_compare_results() # user2's bid compared to user1's

    # another user bids
    user3.bid("auction1", "499")

    # wait for two more compare results
    compa = user3.wait_for_compare_results()
    compb = user3.wait_for_compare_results()

    # can't guarantee which order the two responses arrive in,
    # make sure we print them in the expected order
    if "user1" in compa:
        print compa
        print compb
    else:
        print compb
        print compa

    print user1.wait_for_compare_results() # user1's bid compared to user3's
    print user2.wait_for_compare_results() # user2's bid compared to user3's

    user1.list_auctions() # print user1's view of all auctions so far

    user0.end_auction("auction1") # announce end of bidding

    user0.start_auction("99 bottles of beer", "auction2") # start another auction

    user0.wait_for_outputs(["auction concession", "auction concession", "received a win claim"]) #can't guarantee what order they'll come in

    user0.get_auction_status("auction1") # check status of auction1

    user0.get_bidders("auction1") # determine who's in the running to win

    user0.announce_winner("auction1", "user3", 499) # announce winner
    user1.wait_for_output("received end of auction")
    time.sleep(1)
    user1.list_auctions() # again print user1's view of all auctions so far
    user2.wait_for_output("received end of auction")
    user3.wait_for_output("received end of auction")
    # print all users' view of auction1 outcome
    user1.get_auction_status("auction1")
    user2.get_auction_status("auction1")
    user3.get_auction_status("auction1")
    
    # quit
    user0.quit()
    user1.quit()
    user2.quit()
    user3.quit()

    
if __name__=="__main__":
    main()
