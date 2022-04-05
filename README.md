# Covid-19 visualization

## Setup

Intall docker via https://docs.docker.com/get-docker/

Start the Neo4j server:

``` 
docker-compose -f ./docker-compose.yml up
```

Open Neo4j in browser via link http://localhost:7474/browser/ with
username: neo4j
password: admin

## Stream Person-Visits-Place Data to Neo4j

``` 
./run-stream.sh
```

## Run the Examples

Use following Cypher code to manipulate the constructed graph in Neo4j.

### Set a person to be tested positive

Here id is the selected person's id.

MERGE (test:Test) ON CREATE SET test.status = 'positive'
MERGE (person:Person {id: '1300f777'})
CREATE (test)-[:POSITIVE]->(person)

### Find a group of people with potential risk and push them back to Kafka.

Here id is same as above.

MATCH (p1:Person {id: '1300f777'})
MATCH (p2:Person)
WITH  p1, p2, algo.linkprediction.commonNeighbors(p1, p2) as score
WHERE score >= 0.1
CALL streams.publish('risks', { id:p2.id}) RETURN null

## Do testing

Simulate the testing procedure. Do testing on the people with potential risk and send those test positive back to Neo4j and update the graph.

``` 
./testing.sh
```
