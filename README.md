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

Going Forward
 1. The program still needs to be able to get a complete set of restaurants
    around a given radius of a PF Changs.
 2. The program needs a reliable way to get the coordinates of all of the PF
    Changs (I'm not above manual entry in the short-term!).
 3. The program needs to store the list of all the restaurants within a given
    radius of a given PF Changs in datomic.
 4. The program needs to be set up s.t. when it runs (let's say once a month)
    it updates the datomic database.
 5. Based on the data in datomic we are going to need to get some statistics
    with each PF Changs index so the resultant website can not only provide a 
    PF Chang Index # but also provide some meaningful data s.t. a given PF Changs
    index is understood relative to other PF Changs Indices across the county.
 6. The program needs to be able to spit out a static website that represents
    all of the PF Changs indices that exist (Sadly, w/o PF Changs a PF Changs
    index can't exist). 
 7. The site should have a homepage that allows people to pick an index from a
    predefined list of cities. And some fun information about the cities with the
    highest (BEST) and lowest (WORST) indices. 
 8. Leaning towards a design similar to zagat.com

## Usage
In its current state the program when run just returns a number. This number is 
the PF Chang's Index for a 1000m radius around the PF Changs in a random city.

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
