package io.github.rscai.microservices.inventory.model;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

@Data
@Entity
public class InventoryItem {

  @Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "uuid")
  private String id;
  @Column(length = 255, nullable = false)
  private String productId;
  @Column(nullable = false)
  private int quantity;
  @Column(nullable = false, scale = 2)
  private BigDecimal unitPrice;
  @Column(nullable = false, insertable = true, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date updatedAt;
}
