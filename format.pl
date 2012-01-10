#!/usr/bin/perl
use strict;

# assumes files are in same dir
# usage:
# perl /Users/tholloway/Bluetooth-Chat/format.pl /Users/tholloway/Desktop/map1aFormatted/ > ../data.txt

my $dirname = $ARGV[0];
opendir( DIR, $dirname ) or die "can't opendir $dirname: $!";
while ( defined( my $file = readdir(DIR) ) ) {
	if ( $file =~ m/.*position(\d+)_.*/ ) {
		my $label = 1;
		$label = 0 if ( $1 <= 20 && $1 >= 16 );
		$label = 0 if ( $1 <= 10 && $1 >= 6 );
		open( FILE, $dirname . "/" . $file );
		while ( my $line = <FILE> ) {
			chomp($line);
			my @data = split( "\t", $line );
			print $data[1] . ",$label\n";
		}
		close(FILE);
	}

}
