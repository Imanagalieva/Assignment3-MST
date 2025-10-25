README.md — Assignment 3: Optimization of a City Transportation Network (MST)
Objective

The goal of this assignment is to apply Prim’s and Kruskal’s algorithms to optimize a city’s transportation network by finding the Minimum Spanning Tree (MST) that connects all districts with minimal total cost.

Input Data

The input data (ass_3_input.json) describes a weighted undirected graph where:

Vertices represent city districts.

Edges represent possible roads between them.

Edge weights represent the construction cost.

Example structure:

{
"graphs": [
{
"id": 1,
"nodes": ["A", "B", "C", "D", "E"],
"edges": [
{"from": "A", "to": "B", "weight": 4},
{"from": "A", "to": "C", "weight": 3},
{"from": "B", "to": "C", "weight": 2},
{"from": "B", "to": "D", "weight": 5},
{"from": "C", "to": "D", "weight": 7},
{"from": "C", "to": "E", "weight": 8},
{"from": "D", "to": "E", "weight": 6}
]
}
]
}

Implementation

The project was implemented in Java (Maven project) with the following structure:

Assignment3-MST/
├── src/main/java/org/example/
│   ├── MSTSolver.java         → Main program (reads input, runs algorithms, writes output)
│   ├── PrimAlgorithm.java     → Prim’s algorithm implementation
│   ├── KruskalAlgorithm.java  → Kruskal’s algorithm implementation
│   ├── Graph.java, Edge.java  → Data models
│   └── UnionFind.java         → Helper structure for Kruskal
├── ass_3_input.json
└── ass_3_output_from_run.json

Results Summary
Graph ID	Algorithm	MST Cost	Operations	Time (ms)
1	Prim’s	16	42	1.52
1	Kruskal’s	16	37	1.28
2	Prim’s	6	29	0.87
2	Kruskal’s	6	31	0.92
Analysis & Comparison

Both algorithms produced the same MST total cost, proving correctness.

Prim’s algorithm is efficient on dense graphs (many edges) due to its use of a priority queue and adjacency list.

Kruskal’s algorithm performs better on sparse graphs (few edges), as it sorts edges and uses the Union-Find structure effectively.

The number of operations slightly differs, but both are O(E log V) in complexity.

Execution times were very close, with Kruskal’s being marginally faster for this dataset.

Conclusions

MST total cost is identical for both algorithms, confirming the validity of implementations.

Prim’s algorithm is preferable when the graph is dense and stored as an adjacency list.

Kruskal’s algorithm is more suitable for sparse graphs or when edges are pre-sorted.

Both are highly efficient and scalable for city-scale networks.

References

Cormen, T. H., Leiserson, C. E., Rivest, R. L., & Stein, C. (2009). Introduction to Algorithms (3rd ed.). MIT Press.

GeeksforGeeks: Prim’s Algorithm

GeeksforGeeks: Kruskal’s Algorithm

Result file: ass_3_output_from_run.json
Author: Raushan Imangalieva
Date: October 2025
