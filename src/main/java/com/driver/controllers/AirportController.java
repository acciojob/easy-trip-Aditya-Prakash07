package com.driver.controllers;


import com.driver.Data;
import com.driver.model.Airport;
import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class AirportController {
    @PostMapping("/add_airport")
    public String addAirport(@RequestBody Airport airport){

        //Simply add airport details to your database

        Data.getAirport().add(airport);
        //Return a String message "SUCCESS"

        return "SUCCESS";
    }

    @GetMapping("/get-largest-aiport")
    public String getLargestAirportName(){

        //Largest airport is in terms of terminals. 3 terminal airport is larger than 2 terminal airport
        //Incase of a tie return the Lexicographically smallest airportNam
        try {
            List<Airport> airports = Data.getAirport();
            String largestAirport = "";
            int terminal = -1;
            for (Airport airport : airports) {
                if (airport.getNoOfTerminals() >= terminal) {
                    largestAirport = largestAirport.compareTo(airport.getAirportName()) > 0 ? airport.getAirportName() : largestAirport;
                    terminal = airport.getNoOfTerminals();
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
       return null;
    }

    @GetMapping("/get-shortest-time-travel-between-cities")
    public double getShortestDurationOfPossibleBetweenTwoCities(@RequestParam("fromCity") City fromCity, @RequestParam("toCity")City toCity){

        //Find the duration by finding the shortest flight that connects these 2 cities directly
        //If there is no direct flight between 2 cities return -1.
        double duration = Double.MAX_VALUE;
        try {
            List<Flight> flights = Data.getFlights();
            List<Flight> flights1 = flights.stream().filter(f -> f.getFromCity().equals(fromCity) && f.getToCity().equals(toCity)).collect(Collectors.toList());
            for (Flight flight : flights1) {
                duration = Math.min(flight.getDuration(), duration);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        if(duration == Double.MAX_VALUE)return -1;
        return duration;
    }

    @GetMapping("/get-number-of-people-on-airport-on/{date}")
    public int getNumberOfPeopleOn(@PathVariable("date") Date date,@RequestParam("airportName")String airportName){

        //Calculate the total number of people who have flights on that day on a particular airport
        //This includes both the people who have come for a flight and who have landed on an airport after their flight
        int count = 0;

        try {
            List<Airport> airports = Data.getAirport();
            City city = airports.stream().filter(airport -> airport.getAirportName().equals(airportName)).findFirst().map(Airport::getCity).orElse(null);

            List<Integer> flightIds = Data.getFlights().stream().filter(f -> f.getFlightDate().equals(date) && (f.getFromCity().equals(city) || f.getToCity().equals(city))).map(f -> f.getFlightId()).collect(Collectors.toList());


            Map<Integer, Integer> passengerFlightMap = Data.passengerFlightMap;

            for (Integer passengerId : passengerFlightMap.keySet()) {
                if (flightIds.contains(passengerFlightMap.get(passengerId))) {
                    count++;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return count;
    }

    @GetMapping("/calculate-fare")
    public int calculateFlightFare(@RequestParam("flightId")Integer flightId){

        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight for the third person will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be just checking price
        int bookedPassengerCountInFlight = 0;
        try {

            Map<Integer, Integer> passengerFlightMap = Data.passengerFlightMap;
            for (Integer passengerId : passengerFlightMap.keySet()) {
                if (passengerFlightMap.get(passengerId).equals(flightId)) bookedPassengerCountInFlight++;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return 3000+ (50*bookedPassengerCountInFlight);

    }


    @PostMapping("/book-a-ticket")
    public String bookATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){

        //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case :
        //return a String "FAILURE"
        //Also if the passenger has already booked a flight then also return "FAILURE".
        //else if you are able to book a ticket then return "SUCCESS"
        String FAILURE = "FAILURE";
        String SUCCESS = "SUCCESS";
        try {
            Map<Integer, Integer> passengerFlightMap = Data.passengerFlightMap;
            Map<Integer, Integer> noOfBookingsInFlight = new HashMap<>();
            for (Integer passenger : passengerFlightMap.keySet()) {
                noOfBookingsInFlight.put(passengerFlightMap.get(passenger), noOfBookingsInFlight.getOrDefault(passengerFlightMap.get(passenger), 0) + 1);
            }
            List<Flight> flights = Data.getFlights();
            if (flights.isEmpty()) return FAILURE;
            for (Flight flight : flights) {
                if (flight.getFlightId() == flightId) {
                    if (Objects.equals(passengerFlightMap.get(passengerId), flightId)) return FAILURE;
                    int maxCapacity = flight.getMaxCapacity();
                    if (noOfBookingsInFlight.get(flightId) >= maxCapacity) return FAILURE;
                    passengerFlightMap.put(passengerId, flightId);
                    break;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return SUCCESS;
    }

    @PutMapping("/cancel-a-ticket")
    public String cancelATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){

        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message
        // and also cancel the ticket that passenger had booked earlier on the given flightId
        String FAILURE = "FAILURE";
        String SUCCESS = "SUCCESS";
        try {
            Map<Integer, Integer> passengerFlightMap = Data.passengerFlightMap;
            if (passengerFlightMap.isEmpty() || !passengerFlightMap.get(passengerId).equals(flightId)) return FAILURE;

            for (Integer personId : passengerFlightMap.keySet()) {
                if (passengerFlightMap.get(personId).equals(flightId)) {
                    passengerFlightMap.remove(personId);
                    return SUCCESS;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
       return FAILURE;
    }


    @GetMapping("/get-count-of-bookings-done-by-a-passenger/{passengerId}")
    public int countOfBookingsDoneByPassengerAllCombined(@PathVariable("passengerId")Integer passengerId){

        //Tell the count of flight bookings done by a passenger: This will tell the total count of flight bookings done by a passenger :
        Map<Integer,Integer> passengerFlightMap = Data.passengerFlightMap;
        int count = 0;
        try {

            for (Integer passenger : passengerFlightMap.keySet()) {
                if (passengerFlightMap.containsKey(passenger)) {
                    ++count;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
       return count;
    }

    @PostMapping("/add-flight")
    public String addFlight(@RequestBody Flight flight){

        //Return a "SUCCESS" message string after adding a flight.
        Data.getFlights().add(flight);
       return "SUCCESS" ;
    }


    @GetMapping("/get-aiportName-from-flight-takeoff/{flightId}")
    public String getAirportNameFromFlightId(@PathVariable("flightId")Integer flightId){

        //We need to get the starting airportName from where the flight will be taking off (Hint think of City variable if that can be of some use)
        //return null incase the flightId is invalid or you are not able to find the airportName
        List<Flight> flights = Data.getFlights();
        try {
            Map<City, String> cityAirportMap = Data.getAirport().stream().collect(Collectors.toMap(Airport::getCity, Airport::getAirportName));

            for (Flight flight : flights) {
                if (flight.getFlightId() == flightId) {
                    return cityAirportMap.get(flight.getFromCity());
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }


    @GetMapping("/calculate-revenue-collected/{flightId}")
    public int calculateRevenueOfAFlight(@PathVariable("flightId")Integer flightId){

        //Calculate the total revenue that a flight could have
        //That is of all the passengers that have booked a flight till now and then calculate the revenue
        //Revenue will also decrease if some passenger cancels the flight
        Map<Integer,Integer> passengerFlightMap = Data.passengerFlightMap;
        int count = 0;
        try {
            for (Integer passengerId : passengerFlightMap.keySet()) {
                if (passengerFlightMap.get(passengerId).equals(flightId)) count++;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return 3000*count + 50 *(((count-1)*count)/2);
    }


    @PostMapping("/add-passenger")
    public String addPassenger(@RequestBody Passenger passenger){

        //Add a passenger to the database
        //And return a "SUCCESS" message if the passenger has been added successfully.
        Data.getPassengers().add(passenger);
       return "SUCCESS";
    }


}
