# pfchangsindex

Instructions for calculating a PF Chang's Index.
 1. Find a PF Chang's (google places API) 
 2. Pick a radius
 3. Get the rating of every restaurant within that radius
 4. Order the restaurants by rating.
 5. The PF Chang's index for that area is the PF Chang's element number which is
    the number of restaurants rated more highly than PF Chang's.

It is an approximation of two important variables.
 1. The quality of the restaurants in a city.
 2. The quality of the restaurant culture in a city. Or more directly,
    whether or not the people in a given city have taste.

## Usage

- edit the free-transactor-template.properties file with
  your datomic license.


## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
