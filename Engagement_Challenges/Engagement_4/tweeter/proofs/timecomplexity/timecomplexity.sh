#!/usr/bin/env bash
source ../functions.sh

rm -f ./cookies

echo "Total space used of the input budget is the sum of these: "

createUser a a a 

login a a

echo "This is the worst case:"
time tweetNoLogin "xxxctroencephalographies zeremphasizing xxxctroencephalographies xxxctroencephalographies xxxctroencephalographies xxxctroencephalographies"

echo "These are average messages:"
time tweetNoLogin "IGNITION - High above the Indian Ocean, Fregat started its 2nd burn.4.5 minutes in duration to circularize the orbit http://bit.ly/1U7dmTM"
time tweetNoLogin "You may as well advise people to not use their computer. Make natural behavior safe instead of trying to change it."
time tweetNoLogin "Longtime \"hardware guy\" says Linux Foundation Training \"100% met his expectations.\" Here's why: http://bit.ly/1NyS84F  #learnlinux"
time tweetNoLogin ".@gwynnek  Challenges @fedscoop #mobilegov16 audience to think about how to simplify the complexity of #mobilegov."
time tweetNoLogin "Good morning everyone, wishing you an active day. Don't be a Sheldon. #fitnessday #TuesdayMotivation"

echo "These are average tweets with all words misspelled:"
time tweetNoLogin "XGNITION - Xigh xbove txe Inxian Oxean, Frxgat stxrted ixs 2xd bxrn.4.5 mixutes in durxtion xo cixcularize txe orxit http://bit.ly/1U7dmTM"
time tweetNoLogin "Yxu mxy xx wexl advixe peopxe tx nxt uxe thxir compxter. Maxe natxral behxvior saxe instxad ox tryxng xo chaxge ix."
time tweetNoLogin "Loxgtime \"hardwxre gux\" saxs Lixux Foundaxion Traixing \"100% met hxs expectaxions.\" Hexe's wxy: http://bit.ly/1NyS84F  #learnlinux"
time tweetNoLogin ".@gwynnek  Chaxlenges @fedscoop #mobilegov16 audxence tx thixk abxut hxw tx simxlify txe coxplexity ox #mobilegov."
time tweetNoLogin "Goox moxning evxryone, wixhing yxu ax acxive dxy. Dxn't xe a xheldon. #fitnessday #TuesdayMotivation"

echo "These are average tweets with all words misspelled twice:"
time tweetNoLogin "IxNITxON - Hxgx axxve xxe Ixxian Oxeax, Fxegax stxrtex ixx xxd xuxn.4.5 xinuxes xx dxxation xx cxrculxrize xhx oxbix http://bit.ly/1U7dmTM"
time tweetNoLogin "Yxx xax xx wexx axvixe pexplx xx xxt xsx thxxr comxutxr. Mxxe nxturxl behxvixr sxfx inxtexd xx txyixg xx cxanxe xx."
time tweetNoLogin "Loxxtime \"haxdwxre xxy\" xayx Xxnux Fouxdaxion Trxxning \"100% mxx xxs expextaxions.\" Hexx's xhx: http://bit.ly/1NyS84F  #learnlinux"
time tweetNoLogin ".@gwynnek  Chalxenxes @fedscoop #mobilegov16 audxxnce xx thxxk axoux xox xx simxxify xxe coxplxxity xx #mobilegov."
time tweetNoLogin "Gxod mkrnxng evexxone, wiehiug eou uu aodive dey. ain't ct da Saheeldon. #fitnessday #TuesdayMotivation"

rm -f ./cookies

