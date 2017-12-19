/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.toolkit.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import se.liu.ida.nlp.sdp.toolkit.graph.Graph;
import se.liu.ida.nlp.sdp.toolkit.graph.Node;

/**
 * Read semantic dependency graphs in the SDP 2015 format. The format is
 * specified
 * <a href="http://alt.qcri.org/semeval2015/task18/index.php?id=data-and-tools">here</a>.
 *
 * @author Marco Kuhlmann
 */
public class GraphReader2015 extends ParagraphReader implements GraphReader {

	/**
	 * Create a graph reader, using the default input-buffer size.
	 *
	 * @param reader a Reader object to provide the underlying stream
	 */
	public GraphReader2015(Reader reader) {
		super(reader);
		readFirstLine();
	}

	/**
	 * Create a graph reader that reads from the specified file. The file will
	 * be read using the default input-buffer size.
	 *
	 * @param file the file to read from
	 * @throws FileNotFoundException if the specified file does not exist, is a
	 * directory rather than a regular file, or for some other reason cannot be
	 * opened for reading
	 */
	public GraphReader2015(File file) throws FileNotFoundException {
		super(file);
		readFirstLine();
	}

	/**
	 * Create a graph reader that reads from the specified file. The file will
	 * be read using the default input-buffer size.
	 *
	 * @param fileName the name of the file to read from
	 * @throws FileNotFoundException if the specified file does not exist, is a
	 * directory rather than a regular file, or for some other reason cannot be
	 * opened for reading
	 */
	public GraphReader2015(String fileName) throws FileNotFoundException {
		super(fileName);
		readFirstLine();
	}

	/**
	 * Reads the format identifier line.
	 */
	private void readFirstLine() {
		try {
			String line = super.readLine();
			assert line.equals("#SDP 2015");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Reads a single graph.
	 *
	 * @return the graph read, or {@code null} if the end of the stream has been
	 * reached
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public Graph readGraph() throws IOException {
		List<String> lines = super.readParagraph();
		if (lines == null) {
			return null;
		} else {
			// Every graph should contain at least one token.
			assert lines.size() >= 2;
			// Assert the format of the graph ID.
			assert lines.get(0).matches("#2[0-9]{7}$");

			Graph graph = new Graph(lines.get(0));

			// Add the wall node.
			graph.addNode(Constants.WALL_FORM, Constants.WALL_LEMMA, Constants.WALL_POS, false, false, Constants.WALL_SENSE);

			// Add the token nodes to the graph and collect a list of predicates.
			List<Integer> predicates = new ArrayList<Integer>();
			for (String line : lines.subList(1, lines.size())) {
				String[] tokens = line.split(Constants.COLUMN_SEPARATOR);

				// There should be at least seven columns: ID, FORM, LEMMA, POS, TOP, PRED, SENSE
				assert tokens.length >= 7;
				// Enforce valid values for the TOP column.
				assert tokens[4].equals("+") || tokens[4].equals("-");
				// Enforce valid values for the PRED column.
				assert tokens[5].equals("+") || tokens[5].equals("-");

				String form = tokens[1];
				String lemma = tokens[2];
				String pos = tokens[3];
				boolean isTop = tokens[4].equals("+");
				boolean isPred = tokens[5].equals("+");
				String sense = tokens[6];


				Node node = graph.addNode(form, lemma, pos, isTop, isPred, sense);
				// Make sure that the node ID equals the value of the ID column.
				assert node.id == Integer.parseInt(tokens[0]);

				if (node.isPred) {
					predicates.add(node.id);
				}
			}

			// Add the edges to the graph.
			int id = 1;
			for (String line : lines.subList(1, lines.size())) {
				String[] tokens = line.split(Constants.COLUMN_SEPARATOR);
				// There should be exactly 7 + number of predicates many columns.
				assert tokens.length == 7 + predicates.size();
                                if (tokens.length != 7 + predicates.size()) {
                                    int expected = 7 + predicates.size();
                                    System.out.println(line);
                                    System.out.println("Problematic Line: expected #toks = " + expected + "; Actual: " + tokens.length);
                                }

				for (int i = 7; i < tokens.length; i++) {
					if (!tokens[i].equals(Constants.UNDEFINED)) {
                                            graph.addEdge(predicates.get(i - 7), id, tokens[i]);
					}
				}
				id++;
			}

			// If a node is labeled as a PRED, it should have outgoing edges.
			for (Node node : graph.getNodes()) {
				assert !node.isPred || node.hasOutgoingEdges();
			}

			return graph;
		}
	}
}
