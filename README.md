# Ghidra-VT-Capstone
This is a Version Tracking Tool Plugin for Ghidra which visualizes the VT Match Table at a bird's-eye view.

How to import the plugin:
1) Download the .zip file
2) Open Ghidra and go to Import Extensions
3) Select the .zip file to add the plugin
4) Exit and re-open Ghidra in order to make the plugin available
5) Open a new or previous VT Session and add the plugin when prompted

How to navigate the plugin:
1) Select the three bars icon (leftmost in the set of three) in the top right corner to select a VT Session
2) When prompted, select a VT Session file indicated with the blue footprints icon
     1) You can only select a VT Session in the current project folder directory
     2) If multiple VT Sessions exist in a project folder, then choose one
3) Select the refresh icon (middle in the set of three) to enter Module 1
     1) See Module 1 instructions below
4) Select the scissors icon (rightmost in the set of three) to enter Module 2
     1) See Module 2 instructions below

Module 1 instructions:
1) Three distinct panels will appear with the middle panel being a truncated VT Match Table
2) Select a row in the truncated VT Match Table
3) The left and right panels will turn white except for a single colored line in both panels
4) The location of the color line on the left side corresponds to the relative location of
   the corresponding match in the Source Program (Version 1)
5) The location of the color line on the right side corresponds to the relative location of
   the corresponding match in the Destination Program (Version 2) 
6) The color of both lines indicate the likelihood that the selected match in the VT Match Table is actually a match
     1) Green:  90% - 100% likely to be a match
     2) Yellow: 80% - 89.9% likely to be a match
     3) Orange: 70% - 79.9% likely to be a match
     4) Red:    <70% likely to be a match
7) The truncated VT Match Table can be sorted to easily look for a particular match type, address, or similarity score
8) Ignore the Transform column since this only applies to Module 2

Module 2 instructions:
1) Two distinct panels will appear with a modified VT Match Table on the left side and a new analysis table on the left
2) This module examines the differences in the Destination Program in relation to the Source Program
3) The VT Match Table in this module elimnated redundant or irrelevant matches and reclassified some match correlators
4) The new match tags are seen in the Transform column
     1) True Exact Match: Direct 1 to 1 Function Bytes Match exactly
     2) Instructions: Instruction bytes in a function match exactly
     3) Data: Data bytes in a function match exactly
     4) Compiler: This is a tag for the BSim Function Matching
     5) Yurr: Yet Unexplored Region or Reference
5) Note that only exact bytes matches and compiler similarity analysis was performed
6) The analysis table on the right has three columns (Bytes Similarity, Destination Program Address, Compiler Similarity)
7) How to interpret the Bytes Similarity column
     1) The value in each row is the address delta of where the match appears
         in the Destination Program compared to the Source Program
     2) The color scheme represents the new tag assigned to a bytes-related match
        found at that address seen in the same row in the middle column
             * Dark Green: True Exact Match (All bytes are the same somewhere in the Source Program)
             * Dark Yellow: Instructions (Instruction bytes are the same somewhere in the Source Program)
             * Steel Blue: Data (Data bytes are the same somewhere in the Source Program)
             * Gray: Ignore (No bytes-related tag has been applied to that address)
9) How to interpret the Destination Program Address column
     1) The value in each row is every unique destination address from the VT Match Table
     2) The color for each address represents whether or not an analyzed match was found at
        that address and where it appears relative to the Source Program
        - Gray: Ignore (No analyzed match at this address)
        - White: Match found appears at same address in Source Program
        - Red: Match found appears earlier than match found in Source Program (Can indicate missing code)
        - Blue: Match found appears later than match found in Source Program (Can indicate added code)
        - Strength of Red and Blue indicates how large the difference in addresses are where darker
          shades mean larger differences in addresses in the Source and Destination Programs for a given match
11) How to interpret the Compiler Similarity column
     1)  The value in each row is the address delta of where the match appears in the Destination Program
         as compared to the Source Program
     2) Gray cells can be ignored since no compiler matches were found at those addresses
     3) Purple cells indicate a compiler match was found at that address
     4) The shade of purple indicates the similarity score of the compiler correlator where light purple
        corresponds to a similarity score of 1 and any darker shade is a similarity score that is less than 1
