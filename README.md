# Fenciqi
Streamline textual language study workflow. This is part of a larger project I'm working on for my personal language studies. The idea is to tokenize large volumes of target-language text, quantify the difficulty and domain of the texts, and provide catered content suitable to the user's language abilities and demands. Ideally, this catered content doubly serves as extensive reading material (98% comprehension) and contextual vocabulary review. 

At the moment, the bulk of progress on this work has been made in private, customized for my specific study needs - I've yet to put everything together and re-host it on github. This project will very likely re-manifest as a cloud-backed Flutter app, hopefully in the near future.

# Tokenizing
The tokenizing algorithm present in this code can be used in conjunction with an arbitrary lexicon to tokenize arbitrary text. This being the case, however, it is designed specifically with Mandarin text in mind, and therefore the heuristics employed in the tokenization process will likely have little efficacy applied to other languages.

The general tokenization algorithm, as well as the various heuristics employed in disambiguating tokenizations of apparently equal likelihood, are adopted from MMSEG, written by 蔡志浩 (Chih-Hao Tsai). Both are described below.

Tokenization is chiefly accomplished via a 3-Depth Maximum-Matching Algorithm, the basic axiom of which is that given a string of characters beginning at N, the most likely tokenization is the longest 3-word sequence beginning at N. This depth is not fixed, and can be adjusted as the user pleases, keeping in mind that deeper is not necessarily better.

When multiple 3-word sequences of equal length are found, various heuristics are then employed to resolve disambiguation. These are as implemented as follows and applied in the order described.
1) Greatest average word length
2) Smallest variance of word lengths
3) Largest sum of morphemic freedom of single-character words.
Morphemic freedom here is approximated by frequency count.

# Android Implementation
I initially wrote this as one of many Python scripts in the suite of tools I use in my language studies. Since these tools are primarily used on my desktop, processing speed is not a critical concern. After porting this to Android, I realized that the Trie used to facilitate prefix-searching of words was far too large to populate every time an article was imported. The Trie present in this code is therefore backed by an integer array which is serialized to a binary blob. Deserialization is very quick.
