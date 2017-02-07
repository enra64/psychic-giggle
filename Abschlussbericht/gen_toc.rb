#!/usr/bin/env ruby

# list of words that should be ignored
forbidden_words = ['Table of contents', 'define', 'pragma', 'Inhaltsverzeichnis', 'TODO', '###', '####']

File.open("komplett.md", 'r') do |f|
  f.each_line do |line|
    # ignore non-header lines and lines containing forbidden words
    next if !line.start_with?("#") || forbidden_words.any? { |w| line =~ /#{w}/ }

    # create the title, removing any #, and stripping whitespace
    title = line.gsub("#", "").strip
    
    # remove illegal characters/replace them
    href = title.gsub(" ", "-").gsub(":", "").downcase
    
    # put \tab (number-of-hastags - 1) times, then the title
    puts "    " * (line.count("#")-1) + "* [#{title}](\##{href})"
  end
end
