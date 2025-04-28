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
     a) You can only select a VT Session in the current project folder directory
     b) If multiple VT Sessions exist in a project folder, then choose one
3) Select the refresh icon (middle in the set of three) to enter Module 1
     a) See Module 1 instructions below
4) Select the scissors icon (rightmost in the set of three) to enter Module 2
     a) See Module 2 instructions below

Module 1 instructions:
1) Three distinct panels will appear with the middle panel being a truncated VT Match Table
2) Select a row in the truncated VT Match Table
3) The left and right panels will turn white except for a single colored line in both panels
4) The location of the color line on the left side corresponds to the relative location of
   the corresponding match in the Source Program (Version 1)
5) The location of the color line on the right side corresponds to the relative location of
   the corresponding match in the Destination Program (Version 2) 
6) The color of both lines indicate the likelihood that the selected match in the VT Match Table is actually a match
     a) Green:  90% - 100% likely to be a match
     b) Yellow: 80% - 89.9% likely to be a match
     c) Orange: 70% - 79.9% likely to be a match
     d) Red:    <70% likely to be a match
7) The truncated VT Match Table can be sorted to easily look for a particular match type, address, or similarity score
8) Ignore the Transform column since this only applies to Module 2

Module 2 instructions:
1) Two distinct panels will appear with a modified VT Match Table on the left side and a new analysis table on the left
2) This module examines the differences in the Destination Program in relation to the Source Program
3) The VT Match Table in this module elimnated redundant or irrelevant matches and reclassified some match correlators
4) The new match tags are seen in the Transform column
     a) True Exact Match: Direct 1 to 1 Function Bytes Match exactly
     b) Instructions: Instruction bytes in a function match exactly
     c) Data: Data bytes in a function match exactly
     d) Compiler: This is a tag for the BSim Function Matching
     e) Yurr: Yet Unexplored Region or Reference
5) Note that only exact bytes matches and compiler similarity analysis was performed
6) The analysis table on the right has three columns (Bytes Similarity, Destination Program Address, Compiler Similarity)
7) How to interpret the Bytes Similarity column
     a)  The value in each row is the address delta of where the match appears in the Destination Program compared to the              Source Program
     b) The color scheme represents the new tag assigned to a bytes-related match found at that address seen in the same row          in the middle column
             i) Dark Green: True Exact Match (All bytes are the same somewhere in the Source Program)
             ii) Dark Yellow: Instructions (Instruction bytes are the same somewhere in the Source Program)
             iii) Steel Blue: Data (Data bytes are the same somewhere in the Source Program)
             iv) Gray: Ignore (No bytes-related tag has been applied to that address)
8) How to interpret the Destination Program Address column
     a) The value in each row is every unique destination address from the VT Match Table
     b) The color for each address represents whether or not an analyzed match was found at that address and where it appears         relative to the Source Program
             i) Gray: Ignore (No analyzed match at this address)
             ii) White: Match found appears at same address in Source Program
             iii) Red: Match found appears earlier than match found in Source Program (Can indicate missing code)
             iv) Blue: Match found appears later than match found in Source Program (Can indicate added code)
             v) Strength of Red and Blue indicates how large the difference in addresses are where darker shades mean larger                  differences in addresses in the Source and Destination Programs for a given match
10) How to interpret the Compiler Similarity column
     a)  The value in each row is the address delta of where the match appears in the Destination Program compared to the              Source Program
     b) Gray cells can be ignored since no compiler matches were found at those addresses
     c) Purple cells indicate a compiler match was found at that address
     d) The shade of purple indicates the similarity score of the compiler correlator where light purple corresponds to a             similarity score of 1 and any darker shade is a similarity score that is less than 1
