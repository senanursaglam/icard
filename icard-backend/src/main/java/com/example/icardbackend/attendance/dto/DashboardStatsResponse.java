package com.example.icardbackend.attendance.dto;

import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*Dashboard'ın üst kısmında, backend tarafından hesaplanan aşağıdaki bilgileri gösteren kartlar bulunur. Frontend yalnızca gelen veriyi
görüntüler, hesaplama yapmaz:
Haftalık çalışma saati
Bugünkü çalışma süresi
Bu hafta giriş sayısı
Bu hafta çıkış sayısı
Toplam çalışma süresi*/

public class DashboardStatsResponse {
    private Double weeklyWorkedHours;
    private Double todayWorkedHours;
    private Long weeklyCheckInCount;
    private Long weeklyCheckOutCount;
    private Double totalWorkedHours;
}
