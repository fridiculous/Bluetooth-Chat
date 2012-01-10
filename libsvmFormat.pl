use strict;
my $lineNum=0;
## 664,646,551,547,583,673,352,600,664,536,696,627,527,481,691,432,504,688,540,736,519,504,583,688,455,513,700,480,768,440,1
## <label> <index1>:<value1> <index2>:<value2> ...
while ( my $line = <STDIN> ) {
	chomp($line);
	$lineNum++;
	my @data = split( ",", $line );
	my $label = $data[30];
	print $label;
	for (my $i = 0; $i<30; $i++){
		my $index = $i + 1;
		my $value = $data[$i];
		print " $index:$value";	
	}
	print "\n";
}
