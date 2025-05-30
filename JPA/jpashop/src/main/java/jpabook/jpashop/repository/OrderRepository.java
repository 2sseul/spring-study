package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    @PersistenceContext
    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    // JPA Criteria -> 권장하는 방법 아님, 실무에서 안씀
    public List<Order> findAllByCriteria(OrderSearch orderSearch){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteira = new ArrayList<>();

        if(orderSearch.getOrderStatus() != null){
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteira.add(status);
        }

        if(StringUtils.hasText(orderSearch.getMemberName())){
            Predicate name = cb.like(m.get("name"),"%"+orderSearch.getMemberName()+"%");
            criteira.add(name);
        }

        cq.where(cb.and(criteira.toArray(new Predicate[criteira.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    // 주문 조회 -> 검색이 되어야 함
    public List<Order> findAllByString(OrderSearch orderSearch){

        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        // 주문 상태 검색
        if(orderSearch.getOrderStatus() != null){ // 주문상태가 있을 경우(Order, Cancle)
            if(isFirstCondition){   // 쿼리에 넣는 첫 조건
                jpql += "where";
                isFirstCondition = false;
            }else{  // 아니면 and로 조건 이어가기
                jpql += "and";
            }
        }

        // 회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            if(isFirstCondition){
                jpql += "where";
                isFirstCondition = false;
            }else{
                jpql += "and";
            }
            jpql += "m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class).setMaxResults(1000); // 최대 1000건

        if(orderSearch.getOrderStatus() != null){
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if(StringUtils.hasText(orderSearch.getMemberName())){
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        List<Order> resultList = query.getResultList();
        return resultList;
    }

}
