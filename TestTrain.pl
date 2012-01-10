use strict;
## <label> <index1>:<value1> <index2>:<value2> ...
open (TRAIN, '>train.txt');
open (TEST, '>test.txt');
while ( my $line = <STDIN> ) {
	if (rand() < 0.1){
		print TRAIN $line;	
	}elsif (rand() < 0.01){
		print TEST $line;	
	}

}
close(TRAIN);
close(TEST);