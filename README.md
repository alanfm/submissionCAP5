# Cross-Referencing the Code Smell Oracle with Source Code and Interview Analysis

This project aims to **cross-reference** the *code smell oracle* (`oracle.xlsx`) with the source code available in the *Qualitas Corpus*, as well as to **generate pairs of code samples** used for comparison during the interview phase.

The project includes **refactored code versions** produced manually using the LLM [Qwen2.5-max](https://chat.qwenlm.ai/).

---

## Structure

- `QualitasCorpus-20130901r/`: Repository containing the Java systems from the *Qualitas Corpus*.
- `selected_files/`: Subsets of files selected for analysis, organized by type of code smell.
- `selected_files/dataset-*-r`: Manually refactored files using the LLM [Qwen2.5-max](https://chat.qwenlm.ai/).
- `parseData.py`: Script for data processing and extraction.
- `oracle.xlsx`: Spreadsheet containing the code smell dataset.
- `selected_files/parseFiles.py`: Script for cleaning code and generating paired files.
- `selected_files/generateGroups.py`: Script to generate the code groups used in the interviews.

---

## How to Use

1. Download the [_Qualitas Corpus_](https://qualitascorpus.com/download/) version **20130901r** ([Part 1](http://www.cs.auckland.ac.nz/~ewan/download/corpus/QualitasCorpus-20130901r-pt1.tar) and [Part 2](http://www.cs.auckland.ac.nz/~ewan/download/corpus/QualitasCorpus-20130901r-pt2.tar)).
2. Extract the downloaded files into the `QualitasCorpus-20130901r` directory.
3. Open the `parseData.py` script and configure the directory paths as needed.
4. Run `parseData.py` to process the data and generate the desired cross-references.
5. Afterward, perform the code refactorings with the LLM and save the results in the directories `selected_files/dataset-data-class-r`, `selected_files/dataset-god-class-r`, `selected_files/dataset-long-method-r`, and `selected_files/dataset-feature-envy-r`.
6. Run `parseFiles.py` to generate random pairs of original and refactored code.  
   This will create a directory named `sanitized_java`, containing code pairs with the suffixes `_A.java` and `_B.java`.
7. Run `generateGroups.py` to produce the groups used in the interviews.  
   This will generate a directory with **10 groups**, each containing **5 pairs of code** and corresponding metadata logs in **Markdown (`.md`) format**.

---

## Interview Analysis

### Detailed Semantic Coding

The file [`DetailedSemanticCoding.md`](DetailedSemanticCoding.md) contains the **detailed semantic coding** of the interviews conducted with the study participants.  
It documents the transcripts or summarized statements organized by participant and by code quality criterion (readability, maintainability, modularity, standardization, simplicity, and functionality).  
This file presents the statements that supported the ratings assigned to each criterion, providing **transparency in the qualitative analysis** and **context for the interpretations** discussed in the paper.

### Content Analysis of the Interviews

The file [`ContentAnalysisOfTheInterviews.md`](ContentAnalysisOfTheInterviews.md) presents a **comprehensive content analysis** of the interviews with the seven participants, organized according to the **Bardin (2016)** methodology.  
Its objective is to identify how participants perceive and justify code quality based on six main criteria: readability, maintainability, modularity, standardization, functionality, and simplicity.

### Tables of Choices and Justifications

The file [`ChoicesAndJustifications.md`](ChoicesAndJustifications.md) contains a set of **comparative tables** summarizing the **choices and justifications** made by the seven interviewees (E1–E7) in **five code comparison tasks** — *original versus refactored* versions.
