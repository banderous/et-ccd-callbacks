package uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata;

import lombok.Data;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "judge")
@Data
public class Judge {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private TribunalOffice tribunalOffice;
    private String code;
    private String name;
    @Enumerated(EnumType.STRING)
    private JudgeEmploymentStatus employmentStatus;
}
