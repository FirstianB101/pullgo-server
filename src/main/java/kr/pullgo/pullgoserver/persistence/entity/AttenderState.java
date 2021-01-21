package kr.pullgo.pullgoserver.persistence.entity;

import com.sun.istack.NotNull;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Entity
@Builder
@AllArgsConstructor
public class AttenderState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @NotNull
    @ManyToOne
    private Student attender;

    @ToString.Exclude
    @NotNull
    @ManyToOne
    private Exam exam;

    @Enumerated(EnumType.STRING)
    @NotNull
    private AttendingProgress progress;

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "attenderState", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AttenderAnswer> answers = new HashSet<>();

    private int score;
}
