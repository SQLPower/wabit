log4j.properties.example: . log4j.properties.tail Makefile
	echo "# Log4J configuration file for SQL*Power Wabit" >$@
	echo "# DO NOT EDIT file log4j.properties.example; it is generated automatically." >>$@	
	echo "# Please configure your own logging in a copy named \"log4j.properties\"" >>$@
	echo >> $@
	echo 'log4j.rootLogger=INFO, R, C' >> $@
	echo >> $@
	(cd ../java; find ca -name \*.java | grep -v '\$$' | sort | tr / . | \
		sed -e 's/^/#log4j.logger./' -e 's/\.java$$/=debug/') >>$@
	cat log4j.properties.tail >>$@
