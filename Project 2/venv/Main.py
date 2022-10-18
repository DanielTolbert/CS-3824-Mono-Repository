import igraph as ig

global_graph = ig.Graph()


def graph_setup_pathlinker(file_path):
    graph = ig.Graph()
    with open(file_path) as f:
        line_no = 1
        vertex_set = set(())
        edge_set = set(())
        v_no = 0
        for line in f:
            if line_no > 200:
                break
            if line_no > 1:
                info_array = line.split("\t")
                vertex_set.add(info_array[0])
                vertex_set.add(info_array[1])
                graph.add_vertex(name=info_array[0], community=v_no)
                v_no += 1
                graph.add_vertex(name=info_array[1], community=v_no)
                v_no += 1
                edge_set.add(tuple((info_array[0], info_array[1])))
                graph.add_edge(source=info_array[0], target=info_array[1])
            line_no = line_no + 1
        # for vertex in vertex_set:
    print(graph.summary())
    return graph


def cluster_graph():
    return global_graph.modularity(global_graph.vs["community"])


def nodes_in_community(community):
    return global_graph.vs.select(community_eq=community)


def community_of_node(node):
    return node["community"]


def kronecker_delta(a, b):
    return 1 if a == b else 0


def index_of_module(module_name):
    return len(nodes_in_community(module_name))


def node_in_community(node, community):
    return node[community]


def degree_of(node):
    return global_graph.degree(node)


def move_nodes():
    q_starting = compute_modularity()
    q_current = q_starting
    q_highest = q_current
    for node_u in global_graph.vs:
        starting_community = global_graph.vs[node_u.index]["community"]
        best_community = starting_community
        q_prev = q_highest
        for node_v in node_u.neighbors():
            global_graph.vs[node_u.index]["community"] = global_graph.vs[node_v.index]["community"]  # move communities
            q_current = compute_modularity()
            if q_current > q_highest:
                q_highest = q_current
                best_community = global_graph.vs[node_v.index]["community"]
            else:
                global_graph.vs[node_u.index]["community"] = starting_community  # retain community
        global_graph.vs[node_u.index]["community"] = best_community
    return q_highest - q_starting


def calculate_modules():
    return len(set(global_graph.vs["community"]))


def louvain(threshold):
    delta_q = 1
    prev_q = 0
    while abs(delta_q) >= threshold:
        delta_q = move_nodes() - prev_q
        prev_q = prev_q + delta_q
    return prev_q


def compute_modularity():
    summation = 0
    m = calculate_modules()
    return cluster_graph()
    # for edge in global_graph.es:
    #     node_u = edge.source_vertex
    #     node_v = edge.target_vertex
    #     summation = summation + (1 - ((degree_of(node_u) * degree_of(node_v)) / (2 * m))) \
    #                 * kronecker_delta(index_of_module(community_of_node(node_u)),
    #                                   index_of_module(community_of_node(node_v)))
    # return (summation / (2 * m) * kronecker_delta(summation, global_graph.es[0])) + cluster_graph()


if __name__ == '__main__':
    # global_graph = ig.Graph()
    # global_graph.add_vertex(name="a", community=1)
    # global_graph.add_vertex(name="b", community=1)
    # global_graph.add_vertex(name="c", community=2)
    # global_graph.add_edges([("a", "b"), ("b", "c"), ("c", "a")])
    global_graph = graph_setup_pathlinker("Networks/pathlinker-human-network.txt")
    # louvain_part = global_graph.community_multilevel(weights=None)
    # print(global_graph.modularity(louvain_part, global_graph.vs["community"]))
    print(calculate_modules())
    print(louvain(0.001))
