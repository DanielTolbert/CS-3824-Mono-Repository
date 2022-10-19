import igraph as ig
import matplotlib.pyplot as plt
import time

global_graph = ig.Graph()


def graph_setup_pathlinker(file_path):
    graph = ig.Graph()
    with open(file_path) as f:
        line_no = 1
        vertex_set = set(())
        edge_set = set(())
        v_no = 0
        for line in f:
            if line_no > 20000:
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


def move_nodes_fast():
    queue = list(global_graph.vs)
    q_starting = compute_modularity()
    q_current = q_starting
    q_highest = q_current
    while len(queue) > 0:
        node_u = queue.pop()
        starting_community = global_graph.vs[node_u.index]["community"]
        best_community = starting_community
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


def plot_info(x1, y1, x_name, y_name, x2, y2, legend):
    plt.scatter(x1, y1, c="blue", linewidths=2, marker="x", edgecolors="blue")
    plt.plot(x1, y1, c="blue", label=legend[0])
    plt.scatter(x2, y2, c="red", linewidths=2, marker="o", edgecolors="red")
    plt.plot(x2, y2, c="red", label=legend[1])
    plt.xlabel(x_name)
    plt.ylabel(y_name)
    plt.legend()
    plt.show()


def louvain(threshold):
    start_time = time.time()
    delta_q = 1
    prev_q = 0
    while abs(delta_q) >= threshold:
        curr_q = move_nodes()
        print("{}: {}".format(curr_q, time.time() - start_time))
        delta_q = curr_q - prev_q
        prev_q = prev_q + delta_q
    return prev_q


def leiden():
    start_time = time.time()
    delta_q = 1
    prev_q = 0
    while abs(delta_q) >= threshold:
        curr_q = move_nodes_fast()
        print("{}: {}".format(curr_q, time.time() - start_time))
        delta_q = curr_q - prev_q
        prev_q = prev_q + delta_q
    return prev_q


def compute_modularity():
    summation = 0
    m = calculate_modules()
    for edge in global_graph.es:
        node_u = edge.source_vertex
        node_v = edge.target_vertex
        summation = summation + (1 - ((degree_of(node_u) * degree_of(node_v)) / (2 * m))) \
                    * kronecker_delta(index_of_module(community_of_node(node_u)),
                                      index_of_module(community_of_node(node_v)))
    return (summation / (2 * m) * kronecker_delta(summation, global_graph.es[0])) + cluster_graph()


if __name__ == '__main__':
    louvain_x = [110.6480507850647, 221.54419326782227, 330.93995904922485, 446.01049399375916, 558.342823266983]
    louvain_y = [0.40436079756885596, 0.09717399090615686, 0.017188242531280484, 0.002705011744411623,
                 0.0003351510142636238]

    leiden_x = [0.2349989414215088, 0.4419996738433838, 0.6219985485076904, 0.7959973812103271, 1.009997844696045]
    leiden_y = [0.0011905233608744336, 0.002497195707282347, 0.010413743257717257, 0.06155359249552454,
                0.011316657494027133]
    plot_info(louvain_x, louvain_y, "Time (sec)", "Change in Modularity / CPM", leiden_x, leiden_y, ["Louvain", "Leiden"])
