package com.driver;

import com.driver.model.Airport;
import com.driver.model.Flight;
import com.driver.model.Passenger;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data {

    public static List<Airport> airport = new ArrayList<>();

    public static List<Flight> flights = new ArrayList<>();

    public static List<Passenger> passengers = new ArrayList<>();

    public static Map<Integer,Integer> passengerFlightMap = new HashMap<>();

    public static List<Airport> getAirport() {
        return airport;
    }

    public static void setAirport(List<Airport> airport) {
        Data.airport = airport;
    }

    public static List<Flight> getFlights() {
        return flights;
    }

    public static void setFlights(List<Flight> flights) {
        Data.flights = flights;
    }

    public static List<Passenger> getPassengers() {
        return passengers;
    }

    public static void setPassengers(List<Passenger> passengers) {
        Data.passengers = passengers;
    }
}
