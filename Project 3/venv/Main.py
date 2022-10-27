import igraph

directional_graph = igraph.Graph()
human_annotations = dict()
human_annotations_transferred = dict()
total_genes_set = set()
specific_terms = dict()


class Gene:

    def __init__(self, object_id, qualifier, go_id, evidence_code, aspect):
        self.aspect = aspect
        self.evidence_code = evidence_code
        self.go_id = go_id
        self.qualifier = qualifier
        self.object_id = object_id


def add_relationship_nodes(is_a_relationships, part_of_relationships):
    for go_id in is_a_relationships:
        if len(directional_graph.vs.select(id=go_id)) == 0:
            vert = directional_graph.add_vertex()["id"] = go_id

    for go_id in part_of_relationships:
        if len(directional_graph.vs.select(id=go_id)) == 0:
            vert = directional_graph.add_vertex()["id"] = go_id


def add_relationship_edges(is_a_relationships, part_of_relationships, current_go_id):
    for go_id in is_a_relationships:
        target = directional_graph.vs.select(id=current_go_id)[0].index
        source = directional_graph.vs.select(id=go_id)[0].index
        directional_graph.add_edge(source, target)

    for go_id in part_of_relationships:
        target = directional_graph.vs.select(id=current_go_id)[0].index
        source = directional_graph.vs.select(id=go_id)[0].index
        directional_graph.add_edge(source, target)


def create_nodes_and_edges(is_a_relationships, part_of_relationships, go_id, namespace):
    if len(directional_graph.vs) != 0:
        nodes = directional_graph.vs.select(id=go_id)
    else:
        nodes = []
    vertex = None
    if len(nodes) == 0:  # create a brand new one
        vertex = directional_graph.add_vertex()
        vertex["id"] = go_id
    else:
        vertex = nodes[0]  # Grab existing
    vertex["is_a"] = is_a_relationships
    vertex["part_of"] = part_of_relationships
    vertex["namespace"] = namespace
    add_relationship_nodes(is_a_relationships, part_of_relationships)
    add_relationship_edges(is_a_relationships, part_of_relationships, go_id)


def create_graph(file_path):
    with open(file_path) as f:
        creating_node = False
        is_a_relationships = []
        part_of_relationships = []
        go_id = ""
        namespace = ""
        line_no = 0
        for line in f:
            if line_no >= 1000:
                break
            stringArr = str(line).split(" ")

            if str(line).__len__() == 1:
                if creating_node:
                    create_nodes_and_edges(is_a_relationships, part_of_relationships, go_id, namespace)
                creating_node = False
                # reset
                is_a_relationships = []
                part_of_relationships = []
                go_id = ""
                namespace = ""
            if "[Term]" in line:
                creating_node = True
            if creating_node:
                if stringArr[0] == "is_a:":
                    is_a_relationships.append(stringArr[1])
                elif stringArr[0] == "part_of:":
                    part_of_relationships.append(stringArr[1])
                elif stringArr[0] == "id:":
                    go_id = stringArr[1]
                elif stringArr[0] == "namespace:":
                    namespace = stringArr[1]
            line_no += 1


def parse_go_annotations():
    with open("GeneOntology/goa_human.gaf") as f:
        for line in f:
            if line.startswith("UniProtKB"):
                split_line = line.split("\t")
                if len(split_line) < 17:
                    continue
                obj_id = split_line[1]
                qual = split_line[3]
                go = split_line[4]
                evidence = split_line[6]
                asp = split_line[8]
                gene = Gene(obj_id, qual, go, evidence, asp)
                if human_annotations.get(str(go)) is None:
                    human_annotations[str(go)] = []
                human_annotations[str(go)].append(gene)
                total_genes_set.add(gene)


def find_ancestors(vertex):
    indices = directional_graph.subcomponent(vertex, mode=igraph.OUT)
    ancestors = []
    for index in indices:
        ancestors.append(directional_graph.vs[index])
    return ancestors


def construct_human_annotations_transferred():
    for go_term in directional_graph.vs:
        # get the ancestors
        for ancestor in find_ancestors(go_term):
            # each gene in the ancestor's annotations
            if human_annotations.get(str(go_term["id"])) is None:
                continue
            for gene in human_annotations[str(go_term["id"])]:
                if human_annotations_transferred.get(ancestor["id"]) is None:
                    human_annotations_transferred[ancestor["id"]] = []
                # Create an annotation between gene and ancestor
                human_annotations_transferred[ancestor["id"]].append(gene)


def find_root_term(aspect):
    for vertex in directional_graph.vs:
        if vertex["namespace"] == aspect and len(vertex["is_a"]) == 0 and len(vertex["part_of"]) == 0:
            return vertex
    return None


def genes_annotated_by_root_term(aspect):
    root_term = find_root_term(aspect)
    return len(human_annotations_transferred.get(root_term))


def conditional_terms_in_namespace(namespace, threshold):
    terms = []
    for vertex in directional_graph.vs:
        if len(human_annotations_transferred.get(vertex["id"]) / len(total_genes_set)) > threshold:
            terms.append(vertex)
    return terms


def most_specific_terms_in_S(S):
    terms = []
    for term_u in S:
        include = True
        for term_t in S:
            if term_t != term_u:
                if term_u in find_ancestors(term_t):
                    include = False
                    break
        if include:
            terms.append(term_u)
    return terms


def most_specific_GO_terms():
    nbp = genes_annotated_by_root_term("biological_process")
    nmf = genes_annotated_by_root_term("molecular_function")
    ncc = genes_annotated_by_root_term("cellular_component")
    Sb = conditional_terms_in_namespace("biological_process", nbp / 100)
    Tb = most_specific_terms_in_S(Sb)
    Sm = conditional_terms_in_namespace("molecular_function", nmf / 100)
    Tm = most_specific_terms_in_S(Sm)
    Sc = conditional_terms_in_namespace("cellular_component", ncc / 100)
    Tc = most_specific_terms_in_S(Sc)

    specific_terms["biological_process"] = Tb
    specific_terms["molecular_function"] = Tm
    specific_terms["cellular_component"] = Tc


if __name__ == '__main__':
    create_graph("GeneOntology/go.obo")
    parse_go_annotations()
    construct_human_annotations_transferred()
    most_specific_GO_terms()
