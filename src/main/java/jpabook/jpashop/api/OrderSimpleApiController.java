package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.order.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/sample-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();//Lazy 강제 초기화
            order.getDelivery().getAddress();//Lazy 강제 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/sample-orders")
    public List<SimpleOrderDto> ordersV2() {
        //ORDER 2개
        //N + 1 -> 1 + 화원 N + 배송 N
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<SimpleOrderDto> collect = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return collect;

//        return orderRepository.findAllByString(new OrderSearch()).stream()
//                .map(SimpleOrderDto::new)
//                .collect(toList());
    }

    @GetMapping("/api/v3/sample-orders")// 성능 최적화가 v4 보단 안되지만 재사용성이 있다.
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }
    //두개가 v1, v2 보단 훨씬 빠르지만  - v3, v4 는 그렇게 많이 차이는 안나는 경우가 많다.
    // - 필드내의 조회 갯수 차이보다 join 할때 시간이 많이 걸린다.
    @GetMapping("/api/v4/sample-orders")// 성능 최적화는 좋지만 재사용성이 좋진 않다.
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화
        }
    }


}
