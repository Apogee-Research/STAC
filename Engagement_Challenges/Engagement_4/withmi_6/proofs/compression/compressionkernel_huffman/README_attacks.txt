To verify the time decompression vuln, decompress badTime.cmpr.
From build/install/compression_4/malicious, run
../bin/compressorhost_1 -d  badTime.cmpr -o badTime.dcmp

To verify the space compression vuln due to expansion of certain encodings, compress evil_16.txt
From build/install/compression_2/malicious, run
../bin/compressorhost_1 -c evil_16.txt -o evil_16.dcmp
