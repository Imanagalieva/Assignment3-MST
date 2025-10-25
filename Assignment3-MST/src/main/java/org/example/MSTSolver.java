package org.example;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MSTSolver {

    // --- Data models for JSON I/O ---
    static class InputGraph {
        int id;
        List<String> nodes;
        List<JsonEdge> edges;
    }

    static class JsonEdge {
        String from;
        String to;
        int weight;
    }

    static class ResultsFile {
        List<ResultGraph> results = new ArrayList<>();
    }

    static class ResultGraph {
        int graph_id;
        InputStats input_stats;
        AlgoResult prim;
        AlgoResult kruskal;
    }

    static class InputStats {
        int vertices;
        int edges;
    }

    static class AlgoResult {
        List<JsonEdge> mst_edges;
        int total_cost;
        long operations_count;
        double execution_time_ms;
    }

    // --- Graph internal classes ---
    static class Edge {
        int u, v;
        int w;
        String uName, vName;

        Edge(int u, int v, int w, String uName, String vName) {
            this.u = u;
            this.v = v;
            this.w = w;
            this.uName = uName;
            this.vName = vName;
        }
    }

    // --- Union-Find with operation counting ---
    static class UnionFind {
        int[] p, r;
        long[] opsRef;

        UnionFind(int n, long[] opsRef) {
            this.p = new int[n];
            this.r = new int[n];
            this.opsRef = opsRef;
            for (int i = 0; i < n; i++) p[i] = i;
        }

        int find(int x) {
            opsRef[0]++;
            if (p[x] != x) p[x] = find(p[x]);
            return p[x];
        }

        boolean union(int a, int b) {
            opsRef[0]++;
            int pa = find(a), pb = find(b);
            if (pa == pb) return false;
            if (r[pa] < r[pb]) p[pa] = pb;
            else if (r[pb] < r[pa]) p[pb] = pa;
            else {
                p[pb] = pa;
                r[pa]++;
            }
            opsRef[0]++;
            return true;
        }
    }

    // --- Prim's algorithm ---
    static AlgoResult primMST(int n, List<Edge>[] adj, String[] names) {
        long[] ops = new long[]{0};
        long start = System.nanoTime();

        boolean[] used = new boolean[n];
        int[] minW = new int[n];
        int[] parent = new int[n];
        Arrays.fill(minW, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        minW[0] = 0;
        pq.offer(new int[]{0, 0});
        ops[0]++;

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            ops[0]++;
            int v = cur[1];
            if (used[v]) continue;
            used[v] = true;
            for (Edge e : adj[v]) {
                int to = (e.u == v ? e.v : e.u);
                ops[0]++;
                if (!used[to] && e.w < minW[to]) {
                    minW[to] = e.w;
                    parent[to] = v;
                    pq.offer(new int[]{minW[to], to});
                    ops[0]++;
                } else {
                    ops[0]++;
                }
            }
        }

        List<JsonEdge> mstEdges = new ArrayList<>();
        int total = 0;
        for (int i = 1; i < n; i++) {
            if (parent[i] == -1) continue;
            final int idx = i;
            mstEdges.add(new JsonEdge() {{
                from = names[parent[idx]];
                to = names[idx];
                weight = minW[idx];
            }});
            total += minW[idx];
            ops[0]++;
        }

        long end = System.nanoTime();
        AlgoResult res = new AlgoResult();
        res.mst_edges = mstEdges;
        res.total_cost = total;
        res.operations_count = ops[0];
        res.execution_time_ms = (end - start) / 1_000_000.0;
        return res;
    }

    // --- Kruskal's algorithm ---
    static AlgoResult kruskalMST(int n, List<Edge> edges, String[] names) {
        long[] ops = new long[]{0};
        long start = System.nanoTime();

        edges.sort((e1, e2) -> {
            ops[0]++;
            return Integer.compare(e1.w, e2.w);
        });
        ops[0] += edges.size();

        UnionFind uf = new UnionFind(n, ops);
        List<JsonEdge> result = new ArrayList<>();
        int total = 0;

        for (Edge e : edges) {
            ops[0]++;
            if (uf.union(e.u, e.v)) {
                result.add(new JsonEdge() {{
                    from = e.uName;
                    to = e.vName;
                    weight = e.w;
                }});
                total += e.w;
                ops[0]++;
            } else {
                ops[0]++;
            }
            if (result.size() == n - 1) break;
        }

        long end = System.nanoTime();
        AlgoResult res = new AlgoResult();
        res.mst_edges = result;
        res.total_cost = total;
        res.operations_count = ops[0];
        res.execution_time_ms = (end - start) / 1_000_000.0;
        return res;
    }

    // --- Main driver ---
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java -cp .:gson.jar MSTSolver input.json output.json");
            return;
        }

        String inPath = args[0];
        String outPath = args[1];

        // читаем JSON и разрешаем "мягкий" парсинг
        Reader reader = Files.newBufferedReader(Paths.get(inPath));
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);

        JsonElement rootElement = JsonParser.parseReader(jsonReader);
        Gson gson = new Gson();

        // поддержка обоих форматов JSON
        JsonArray graphs;
        if (rootElement.isJsonObject() && rootElement.getAsJsonObject().has("graphs")) {
            graphs = rootElement.getAsJsonObject().getAsJsonArray("graphs");
        } else if (rootElement.isJsonArray()) {
            graphs = rootElement.getAsJsonArray();
        } else {
            throw new IllegalArgumentException("Invalid JSON format: expected object with 'graphs' or array of graphs");
        }

        ResultsFile out = new ResultsFile();

        for (JsonElement ge : graphs) {
            JsonObject g = ge.getAsJsonObject();
            int id = g.get("id").getAsInt();
            JsonArray nodes = g.getAsJsonArray("nodes");
            int n = nodes.size();
            String[] names = new String[n];
            Map<String, Integer> nameToIdx = new HashMap<>();
            for (int i = 0; i < n; i++) {
                names[i] = nodes.get(i).getAsString();
                nameToIdx.put(names[i], i);
            }

            JsonArray edgesJson = g.getAsJsonArray("edges");
            List<Edge> edgeList = new ArrayList<>();
            List<Edge>[] adj = new ArrayList[n];
            for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();

            for (JsonElement ee : edgesJson) {
                JsonObject eo = ee.getAsJsonObject();
                String f = eo.get("from").getAsString();
                String t = eo.get("to").getAsString();
                int w = eo.get("weight").getAsInt();
                int ui = nameToIdx.get(f);
                int vi = nameToIdx.get(t);
                Edge e = new Edge(ui, vi, w, f, t);
                edgeList.add(e);
                adj[ui].add(e);
                adj[vi].add(e);
            }

            AlgoResult primRes = primMST(n, adj, names);
            AlgoResult kruskalRes = kruskalMST(n, edgeList, names);

            ResultGraph rg = new ResultGraph();
            rg.graph_id = id;
            rg.input_stats = new InputStats();
            rg.input_stats.vertices = n;
            rg.input_stats.edges = edgeList.size();
            rg.prim = primRes;
            rg.kruskal = kruskalRes;
            out.results.add(rg);
        }

        Gson gsonOut = new GsonBuilder().setPrettyPrinting().create();
        String outJson = gsonOut.toJson(out);
        Files.write(Paths.get(outPath), outJson.getBytes());
        System.out.println("✅ Results written to " + outPath);
    }
}
