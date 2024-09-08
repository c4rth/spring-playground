package org.c4rth.jpah2.db;



import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "TEST_TABLE")
@ToString
public class DummyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="EXTENDED_NAME")
    private String name;
}