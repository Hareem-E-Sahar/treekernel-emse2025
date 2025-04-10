# TreeKernel-EMSE2025

This repository contains the **replication package** for the paper:

**An Empirical Evaluation of Tree Kernels for Source Code Retrieval**, to appear in *EMSE 2025*.

Follow the instructions below to reproduce the experiments and results from the paper.

---

## Repository Structure

- `data/` – Dataset and AST files.
- `code/` – Source code for running all experiments.
- `results/` – Output metrics from running the experiments.

---

## Step 1: Clone the Repository

```bash
git clone https://github.com/your-username/treekernel-emse2025.git
cd treekernel-emse2025
```

---

## Step 2: Explore the Contents

### `data/` directory

This folder includes the **BigCloneBench** dataset and derived artifacts:

- `bcb_reduced/` – Java source files used as the base for the benchmark.
- `functionStr/` – Extracted methods/code fragments, parsed into ASTs using the [GumTree library](https://github.com/GumTreeDiff/gumtree).
- `sexpr_from_gumtree/` – ASTs in S-expression format for tree kernel consumption.
- `TestH2Database/` – Ground truth clone pairs stored in `bigclonedb_clones_alldir_8584153.txt`. Alternatively you can  download the ground truth file [here](https://drive.google.com/file/d/15N9kWtV4TMe-uxXcH0doi1Bbn3vbllbX/view?usp=sharing).

- `checkstyle_complexity_all.csv` – McCabe’s Cyclomatic Complexity scores, computed using [Checkstyle](https://checkstyle.sourceforge.io/checks/metrics/cyclomaticcomplexity.html).

---

### `code/` directory

This directory contains all code for reproducing the results.

Each research question (RQ) is mapped to a dedicated subdirectory:

- **RQ1:** Evaluates and compares three tree kernels (PTK, STK, SSTK) against a TF-IDF baseline.
- **RQ2:** Compares tree kernels across different clone types (Type-1, Type-2, and Type-3), and also against nicad clone detector which is the baseline.
- **RQ3:** Assesses performance across code fragments of varying sizes (LOC) and complexities (Cyclomatic Complexity).
- **RQ4:** Evaluates a hybrid model that combines TF-IDF and tree kernels for improved runtime.

---

## Requirements

### 1. Set Up Elasticsearch (for RQ1 baseline and RQ4 hybrid)

- Download and install Elasticsearch from [elastic.co](https://www.elastic.co/downloads/elasticsearch).
- Start the server:

```bash
cd elasticsearch-x.x.x/bin
./elasticsearch
```

Ensure it remains running before starting the relevant experiments.

### 2. Index data into Elasticsearch
- You need to index the code fragments into Elasticsearch for RQ1 baseline and RQ4 hybrid model.

- To do this, navigate to the `./code/rq4/hybrid/src/main/java/sample/evaluation/elasticsearch` directory and run:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.elasticsearch.ESFileIndexer"
```

---

## Running the Experiments

### RQ1: Tree Kernel vs TF-IDF Baseline

**Tree Kernel Evaluation**

```bash
cd code/kelp-full
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.kelp.TKBigCloneEval"
```

**TF-IDF Baseline (Requires Elasticsearch Running)**

```bash
cd code/tfidf/es-code
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.elasticsearch.ElasticsearchBigCloneEval"
```

---

### RQ2: Syntactic Type Analysis

```bash
cd code/kelp-full
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.kelp.TKBigCloneEval"
```
**Nicad Baseline**

- To evaluate the **NiCad** clone detector, follow the setup and execution steps provided in the BigCloneEval repository. [BigCloneEval framework](https://github.com/jeffsvajlenko/BigCloneEval)
-  After running the evaluation, save the results to: ./data/BigCloneEval/result.
-  Run the Python script below to generate metrics:  ./code/rq2/nicad/main.py 


---

### RQ3: Code Complexity and Size Analysis

```bash
cd code/kelp-full
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.kelp.TKBigCloneEval"
```

---

### RQ4: Hybrid Tree Kernel + TF-IDF Model (Requires Elasticsearch Server Running)

```bash
cd code/rq4/hybrid
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.eskelp.ElasticsearchKelp"
```

---

## `results/` directory

This folder contains the output results and metrics from the experiments, including accuracy scores, runtime, and evaluation logs. These can be used to verify the findings reported in the paper.

---

## Contact

For questions or feedback, feel free to open an issue or contact the authors via the corresponding email provided in the paper.
