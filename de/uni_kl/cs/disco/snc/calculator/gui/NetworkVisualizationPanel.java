/*
 *  (c) 2017 Michael A. Beck, Sebastian Henningsen
 *  		disco | Distributed Computer Systems Lab
 *  		University of Kaiserslautern, Germany
 *  All Rights Reserved.
 *
 * This software is work in progress and is released in the hope that it will
 * be useful to the scientific community. It is provided "as is" without
 * express or implied warranty, including but not limited to the correctness
 * of the code or its suitability for any particular purpose.
 *
 * This software is provided under the MIT License, however, we would 
 * appreciate it if you contacted the respective authors prior to commercial use.
 *
 * If you find our software useful, we would appreciate if you mentioned it
 * in any publication arising from the use of this software or acknowledge
 * our work otherwise. We would also like to hear of any fixes or useful
 */
package de.uni_kl.cs.disco.snc.calculator.gui;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.Dimension;
import java.awt.ScrollPane;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.apache.commons.collections15.Transformer;

import de.uni_kl.cs.disco.snc.calculator.SNC;
import de.uni_kl.cs.disco.snc.calculator.network.Flow;
import de.uni_kl.cs.disco.snc.calculator.network.Network;
import de.uni_kl.cs.disco.snc.calculator.network.NetworkListener;
import de.uni_kl.cs.disco.snc.calculator.network.Vertex;

/**
 * A panel which uses a graph library to display a network.
 *
 * @author Sebastian Henningsen
 */
public class NetworkVisualizationPanel {

    //private final JScrollPane visualizationPanel;
    private GraphZoomScrollPane visualizationPanel;
    private Graph<GraphItem, GraphItem> graph;
    private Layout<GraphItem, GraphItem> layout;
    VisualizationViewer<GraphItem, GraphItem> bvs;
    private Dimension size;
    private List<GraphItem> vertices;
    private List<GraphItem> flows;
    private final double attractionMultiplier = 0.5;
    private final double repulsionMultiplier = 1;

    /**
     * Creates the panel.
     *
     * @param size
     */
    public NetworkVisualizationPanel(Dimension size) {
        vertices = new LinkedList<>();
        flows = new LinkedList<>();
        this.size = size;
        graph = new SparseMultigraph();

        layout = new FRLayout<>(graph);
        ((FRLayout) layout).setAttractionMultiplier(attractionMultiplier);
        ((FRLayout) layout).setRepulsionMultiplier(repulsionMultiplier);
        layout.setSize(size);
        bvs = new VisualizationViewer<>(layout);
        bvs.setPreferredSize(size);
        bvs.getRenderContext().setVertexLabelTransformer(new Transformer<GraphItem, String>() {
            @Override
            public String transform(GraphItem i) {
                return i.toString();
            }
        });
        bvs.getRenderContext().setEdgeLabelTransformer(new Transformer<GraphItem, String>() {
            @Override
            public String transform(GraphItem i) {
                return i.toString();
            }
        });
        bvs.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        visualizationPanel = new GraphZoomScrollPane(bvs);
        visualizationPanel.setPreferredSize(size);

        final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        bvs.setGraphMouse(graphMouse);

        //visualizationPanel.add(bvs);
        SNC.getInstance().registerNetworkListener(new NetworkChangeListener());
    }

    /**
     * Returns the Panel on which everything is displayed.
     *
     * @return
     */
    public JPanel getPanel() {
        return visualizationPanel;
    }

    private class NetworkChangeListener implements NetworkListener {

        private void updateLayout() {
            bvs.getGraphLayout().setGraph(graph);
            //layout = new FRLayout<>(graph);
            //bvs.setGraphLayout(layout);
            bvs.repaint();
        }

        @Override
        public void vertexAdded(Vertex newVertex) {
            GraphItem gi = new GraphItem(newVertex.getID(), newVertex.getAlias());
            graph.addVertex(gi);
            vertices.add(gi);
            updateLayout();
        }

        @Override
        public void vertexRemoved(Vertex removedVertex) {
            GraphItem gi = new GraphItem(removedVertex.getID(), removedVertex.getAlias());
            vertices.remove(gi);
            graph.removeVertex(gi);
            // TODO: Handle flows. Is this already covered by flowChanged()?
            updateLayout();
        }

        @Override
        public void flowAdded(Flow newFlow) {
            List<Integer> route = newFlow.getVerticeIDs();
            Iterator<Integer> it = route.iterator();
            int oldId = it.next();
            if (route.size() > 1) {
                int newID = 0;
                int i = 0;
                while (it.hasNext()) {
                    newID = it.next();
                    System.out.println(oldId + " " + newID);
                    GraphItem gi = new GraphItem(newFlow.getID() + i, newFlow.getAlias());
                    flows.add(gi);
                    graph.addEdge(gi, getVertexbyID(oldId), getVertexbyID(newID), EdgeType.DIRECTED);
                    oldId = newID;
                    i++;
                }
            } else {
                GraphItem gi = new GraphItem(newFlow.getID(), newFlow.getAlias());
                flows.add(gi);
                graph.addEdge(gi, getVertexbyID(oldId), getVertexbyID(oldId), EdgeType.DIRECTED);
            }
            updateLayout();
        }

        @Override
        public void flowRemoved(Flow removedFlow) {
            List<Integer> route = removedFlow.getVerticeIDs();
            for (int i = 0; i < route.size(); i++) {
                GraphItem gi = getFlowbyID(removedFlow.getID() + i);
                graph.removeEdge(gi);
                flows.remove(gi);
            }
            updateLayout();

        }

        @Override
        public void flowChanged(Flow changedFlow) {
            // Only the route is relevant for us
            // Remove the old one and add new ones
            for (GraphItem gi : getFlowbyAlias(changedFlow.getAlias())) {
                graph.removeEdge(gi);
                flows.remove(gi);
            }
            flowAdded(changedFlow);

        }

        @Override
        public void vertexChanged(Vertex changedVertex) {
            // Ignore for now
        }

        @Override
        public void clear() {
            graph = new SparseMultigraph<>();
            updateLayout();
        }

        private GraphItem getVertexbyID(int id) {
            GraphItem result = null;
            for (GraphItem gi : vertices) {
                if (gi.getID() == id) {
                    result = gi;
                }
            }
            return result;
        }

        private GraphItem getFlowbyID(int id) {
            GraphItem result = null;
            for (GraphItem gi : flows) {
                if (gi.getID() == id) {
                    result = gi;
                }
            }
            return result;
        }

        private List<GraphItem> getFlowbyAlias(String alias) {
            List<GraphItem> result = new LinkedList<>();
            for (GraphItem gi : flows) {
                if (gi.getAlias().equals(alias)) {
                    result.add(gi);
                }
            }
            return result;
        }

    }
}

class GraphItem {

    private final int id;
    private final String alias;

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.id;
        hash = 79 * hash + Objects.hashCode(this.alias);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GraphItem other = (GraphItem) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.alias, other.alias)) {
            return false;
        }
        return true;
    }

    public GraphItem(int id, String alias) {
        this.id = id;
        this.alias = alias;
    }

    @Override
    public String toString() {
        return alias;
    }

    public int getID() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

}

/*class VisibilityPredicate<V extends GraphItem, E extends GraphItem> implements Predicate<Context<Graph<V, E>, V>> {

    @Override
    public boolean evaluate(Context<Graph<V, E>, V> context) {
        Graph<V, E> g = context.graph;
        V v = context.element;
        return v.isVisible();
    }
}*/
