import requests
import json
import random
import time

def send_rabbit_care_location(name, latitude, longitude):
    """
    Simulira slanje poruke o lokaciji na Message Queue broker.
    """
    # URL endpoint vašeg brokera za slanje Direct poruka
    broker_url = "http://localhost:8081/queue/send/direct/rabbit-care-exchange/location.update"

    # Podaci o lokaciji
    location_data = {
        "name": name,
        "latitude": latitude,
        "longitude": longitude
    }

    headers = {'Content-Type': 'application/json'}

    try:
        response = requests.post(broker_url, data=json.dumps(location_data), headers=headers)
        if response.status_code == 200:
            print(f"✅ Uspešno poslata lokacija: '{name}'")
        else:
            print(f"❌ Greška pri slanju lokacije. Status kod: {response.status_code}")
            print(f"Odgovor servera: {response.text}")

    except requests.exceptions.RequestException as e:
        print(f"❌ Nije moguće povezati se sa brokerom. Proverite da li je aplikacija pokrenuta na portu 8081.")
        print(f"Greška: {e}")

def main():
    """
    Glavna funkcija koja šalje nekoliko testnih poruka.
    """
    print("--- Pokretanje simulacije aplikacije za brigu o zečevima ---")
    
    locations = [
        ("Azil 'Zeka Zvonko'", 45.242, 19.845),
        ("Veterinar 'Šapice'", 45.253, 19.825),
        ("Sklonište za zečeve 'Medeno srce'", 45.260, 19.835)
    ]

    for name, lat, lon in locations:
        send_rabbit_care_location(name, lat, lon)
        # Pauza pre slanja sledeće poruke
        time.sleep(random.randint(1, 3))
        
    print("\n--- Simulacija završena. ---")

if __name__ == "__main__":
    main()