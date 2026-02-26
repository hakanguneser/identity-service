package com.gastroblue.model.enums;

import static com.gastroblue.model.enums.Country.GERMANY;
import static com.gastroblue.model.enums.Country.TURKEY;

import com.gastroblue.model.base.ConfigurableEnum;
import java.util.Arrays;
import java.util.List;

public enum City implements ConfigurableEnum {
  ADANA(TURKEY),
  ADIYAMAN(TURKEY),
  AFYONKARAHISAR(TURKEY),
  AGRI(TURKEY),
  AMASYA(TURKEY),
  ANKARA(TURKEY),
  ANTALYA(TURKEY),
  ARTVIN(TURKEY),
  AYDIN(TURKEY),
  BALIKESIR(TURKEY),
  BILECIK(TURKEY),
  BINGOL(TURKEY),
  BITLIS(TURKEY),
  BOLU(TURKEY),
  BURDUR(TURKEY),
  BURSA(TURKEY),
  CANAKKALE(TURKEY),
  CANKIRI(TURKEY),
  CORUM(TURKEY),
  DENIZLI(TURKEY),
  DIYARBAKIR(TURKEY),
  EDIRNE(TURKEY),
  ELAZIG(TURKEY),
  ERZINCAN(TURKEY),
  ERZURUM(TURKEY),
  ESKISEHIR(TURKEY),
  GAZIANTEP(TURKEY),
  GIRESUN(TURKEY),
  GUMUSHANE(TURKEY),
  HAKKARI(TURKEY),
  HATAY(TURKEY),
  ISPARTA(TURKEY),
  MERSIN(TURKEY),
  ISTANBUL(TURKEY),
  IZMIR(TURKEY),
  KARS(TURKEY),
  KASTAMONU(TURKEY),
  KAYSERI(TURKEY),
  KIRKLARELI(TURKEY),
  KIRSEHIR(TURKEY),
  KOCAELI(TURKEY),
  KONYA(TURKEY),
  KUTAHYA(TURKEY),
  MALATYA(TURKEY),
  MANISA(TURKEY),
  KAHRAMANMARAS(TURKEY),
  MARDIN(TURKEY),
  MUGLA(TURKEY),
  MUS(TURKEY),
  NEVSEHIR(TURKEY),
  NIGDE(TURKEY),
  ORDU(TURKEY),
  RIZE(TURKEY),
  SAKARYA(TURKEY),
  SAMSUN(TURKEY),
  SIIRT(TURKEY),
  SINOP(TURKEY),
  SIVAS(TURKEY),
  TEKIRDAG(TURKEY),
  TOKAT(TURKEY),
  TRABZON(TURKEY),
  TUNCELI(TURKEY),
  SANLIURFA(TURKEY),
  USAK(TURKEY),
  VAN(TURKEY),
  YOZGAT(TURKEY),
  ZONGULDAK(TURKEY),
  AKSARAY(TURKEY),
  BAYBURT(TURKEY),
  KARAMAN(TURKEY),
  KIRIKKALE(TURKEY),
  BATMAN(TURKEY),
  SIRNAK(TURKEY),
  BARTIN(TURKEY),
  ARDAHAN(TURKEY),
  IGDIR(TURKEY),
  YALOVA(TURKEY),
  KARABUK(TURKEY),
  KILIS(TURKEY),
  OSMANIYE(TURKEY),
  DUZCE(TURKEY),

  // --- Almanya (örnek şehirler) ---
  BERLIN(GERMANY),
  FRANKFURT(GERMANY),
  MUNICH(GERMANY),
  KOELN(GERMANY);

  private final Country country;

  City(Country country) {
    this.country = country;
  }

  public Country country() {
    return country;
  }

  public boolean isIn(Country c) {
    return this.country == c;
  }

  public static List<City> of(Country country) {
    return Arrays.stream(values()).filter(c -> c.country == country).toList();
  }
}
