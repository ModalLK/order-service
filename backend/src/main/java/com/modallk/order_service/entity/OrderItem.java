package com.modallk.order_service.entity;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private String size;
    private Double subtotal;
}
