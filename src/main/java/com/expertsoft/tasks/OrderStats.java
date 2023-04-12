package com.expertsoft.tasks;

import com.expertsoft.model.*;
import com.expertsoft.util.AveragingBigDecimalCollector;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class provides several methods to collect statistical information from customers and orders of an e-shop.
 * Your task is to implement methods of this class using Java 8 Stream API.
 * Each method is covered with a number of unit tests. You can use <code>mvn test</code> call to check your implementation.
 *
 * Refer to the <code>com.expertsoft.model</code> package to observe the domain model of the shop.
 */
class OrderStats {

    /**
     * Task 1 (⚫⚫⚪⚪⚪)
     *
     * Given a stream of customers, return the list of orders, paid using provided credit card type (Visa or MasterCard).
     *
     * @param customers stream of customers
     * @param cardType credit card type
     * @return list, containing orders paid with provided card type
     */
    static List<Order> ordersForCardType(final Stream<Customer> customers, PaymentInfo.CardType cardType) {

        //we use flatMap for nested lists
        return customers
                .flatMap(m -> m.getOrders().stream())
                .filter(f -> f.getPaymentInfo().getCardType().equals(cardType))
                .collect(Collectors.toList());
    }


    /**
     * Task 2 (⚫⚫⚪⚪⚪)
     *
     * Given a stream of orders, return a map, where keys are different order sizes and values are lists of orders,
     * referring to this sizes. Order size here is just a total number of products in the order.
     *
     * @param orders stream of orders
     * @return map, where order size values mapped to lists of orders
     */
    static Map<Integer, List<Order>> orderSizes(final Stream<Order> orders) {
        //The sum() method is available in the primitive int-value stream like IntStream, not Stream<Integer>.
        // We can use mapToInt() to convert a stream integers into a IntStream.
        return orders.collect(
                Collectors.groupingBy(g -> g.getOrderItems()
                        .stream()
                        .mapToInt(i -> i.getQuantity())
                        .sum())
        );
    }


    /**
     * Task 3 (⚫⚫⚫⚪⚪)
     *
     * Given a stream of orders, return true only if EVERY order in the stream contains at least
     * one product of the provided color and false otherwise.
     *
     * @param orders stream of orders
     * @param color product color to test
     * @return boolean, representing if every order in the stream contains product of specified color
     */
    static Boolean hasColorProduct(final Stream<Order> orders, final Product.Color color) {

        //Funciona solo en el primer caso (Analiza solo el bloque de Producto)
//        return orders.flatMap(g -> g.getOrderItems().stream())
//                .anyMatch(s -> s.getProduct().getColor().equals(color));

        //Funciona solo en el segundo caso (Analiza todos los bloques)
//        return orders.flatMap(g -> g.getOrderItems().stream())
//                .allMatch(s -> s.getProduct().getColor().equals(color));

        //Funciona en ambos casos
        return orders.allMatch(o -> o.getOrderItems()
                .stream()
                .anyMatch(i -> i.getProduct().getColor().equals(color)));

    }


    /**
     * Task 4 (⚫⚫⚫⚫⚪)
     *
     * Given a stream of customers, return the map, where customer email is mapped to a number of different credit cards he/she used by the customer.
     *
     * @param customers stream of customers
     * @return map, where for each customer email there is a long referencing a number of different credit cards this customer uses.
     */
        //Email / Credit Card
    static Map<String, Long> cardsCountForCustomer(final Stream<Customer> customers) {


//        Map<String, Integer> map = users.stream()
//                .collect(Collectors.toMap(User::getName, User::getAge));


        //Streams are for a list of elements
        //As it just asked for the number of credit cards we use .count
       return customers.collect(Collectors.toMap(m -> m.getEmail(),
               m -> m.getOrders()
                       .stream()
                       .map(f -> f.getPaymentInfo())
                       .map(n -> n.getCardNumber())
                       .distinct()
                       .count()

                ));

    }

    /**
     * Task 5 (⚫⚫⚫⚫⚫)
     *
     * Given a stream of customers, return the optional, containing the most popular country name,
     * that is, the name of the country set in addressInfo by the biggest amount of customers.
     * If there are two or more countries with the same amount of customers, return the country name that has a smallest length.
     * If customer stream is empty, Optional.empty should be returned.
     *
     * Example: For the stream, containing
     *      Customer#1 -> USA
     *      Customer#2 -> France
     *      Customer#3 -> Japan
     *      Customer#4 -> USA
     *      Customer#5 -> Japan
     *
     *      "USA" should be returned.
     *
     * @param customers stream of customers
     * @return java.util.Optional containing the name of the most popular country
     */
    static Optional<String> mostPopularCountry(final Stream<Customer> customers) {

        Map<String, Long> optionalMap = customers.collect(Collectors.groupingBy(c -> c.getAddress().getCountry(),
                Collectors.mapping(customer -> customer.getAddress().getCountry(), Collectors.counting())));

        Optional optional = optionalMap.entrySet()
                .stream()
                .max(Comparator.comparing(value -> value.getValue()))
                .map(key -> key.getKey());

        return optional;
    }


    /**
     * Task 6 (⚫⚫⚫⚫⚫)
     *
     * Given a stream of customers, return the average product price for the provided credit card number.
     *
     * Info: If order contains the following order items:
     *  [
     *      Product1(price = 100$, quantity = 2),
     *      Product2(price = 160$, quantity = 1)
     *  ]
     * then the average product price for this order will be 120$ ((100 * 2 + 160 * 1) / 3)
     *
     * Hint: Since product prices are represented as BigDecimal objects, you are provided with the collector implementation
     *       to compute the average value of BigDecimal stream.
     *
     * @param customers stream of customers
     * @param cardNumber card number to check
     * @return average price of the product, ordered with the provided card
     */
    static BigDecimal averageProductPriceForCreditCard(final Stream<Customer> customers, final String cardNumber) {
        final AveragingBigDecimalCollector collector = new AveragingBigDecimalCollector();

        //IntStream is a sequence of primitive int-value elements and a specialized stream for manipulating int values.
        //rangeClosed() is also used to generate the numbers in the order with incremental by one
        // but it includes the end index of this method.
        return customers.flatMap(f -> f.getOrders().stream())
                .filter(n -> n.getPaymentInfo().getCardNumber().equals(cardNumber))
                .flatMap(fm -> fm.getOrderItems().stream())
                .flatMap(range -> IntStream.rangeClosed(1, range.getQuantity()).mapToObj(num -> range.getProduct()))
                .map(price -> price.getPrice())
                .collect(collector);

    }
}
