package com.limuzi.limuziaicodemother.langgraph4j.demo;

import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.state.Channel;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;

import java.awt.image.SampleModel;
import java.util.List;
import java.util.Map;

public class SimpleGraphApp {

    public static void main(String[] args) throws GraphStateException {
        // Initialize nodes

        GreeterNode greeterNode = new GreeterNode();
        ResponderNode responderNode = new ResponderNode();

        Map<String, Channel<?>> schema = SimpleState.SCHEMA;

        // Define the graph structure
       var stateGraph = new StateGraph<>(SimpleState.SCHEMA, SimpleState::new)
            .addNode("greeter", node_async(greeterNode))
            .addNode("responder", node_async(responderNode))
            // Define edges
            .addEdge(START, "greeter") // Start with the greeter node
            .addEdge("greeter", "responder")
            .addEdge("responder", END)   // End after the responder node
             ;
        // Compile the graph
        var compiledGraph = stateGraph.compile();

        GraphRepresentation demo = stateGraph.getGraph(GraphRepresentation.Type.MERMAID, "demo");
        System.out.println(demo);

        // Run the graph
        // The `stream` method returns an AsyncGenerator.
        // For simplicity, we'll collect results. In a real app, you might process them as they arrive.
        // Here, the final state after execution is the item of interest.

        for (var item : compiledGraph.stream( Map.of( SimpleState.MESSAGES_KEY, "Let's, begin!" ) ) ) {

            System.out.println( item );
        }

    }
}