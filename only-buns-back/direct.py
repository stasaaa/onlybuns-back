import requests

# API endpoint (replace with your actual Spring Boot server address)
api_url = "http://localhost:8080/queue/send/direct/rabbit-care"

def get_user_input(prompt):
    value = input(prompt)
    return value.strip()

def get_location_data():
    print("Enter Rabbit Care Location Details:")
    name = get_user_input("Name: ")
    country = get_user_input("Country: ")
    postal_code = get_user_input("Postal Code: ")
    city = get_user_input("City: ")
    street = get_user_input("Street: ")
    number = get_user_input("Street Number: ")

    # Input latitude and longitude with basic validation
    while True:
        try:
            latitude = float(get_user_input("Latitude: "))
            longitude = float(get_user_input("Longitude: "))
            break
        except ValueError:
            print("Invalid input! Please enter valid numeric values for latitude and longitude.")

    # Construct the RabbitCareLocation JSON payload
    location_data = {
        "name": name,
        "address": {
            "country": country,
            "postalCode": postal_code,
            "city": city,
            "street": street,
            "number": number,
            "latitude": latitude,
            "longitude": longitude
        }
    }

    return location_data

def main():
    location_data = get_location_data()

    try:
        # Send a POST request to your Spring Boot endpoint
        response = requests.post(api_url, json=location_data)

        # Check the response
        if response.status_code == 200:
            print("✅ Message sent successfully!")
            print(response.text)
        else:
            print(f"❌ Failed to send message. Status code: {response.status_code}")
            print(response.text)

    except Exception as e:
        print(f"⚠️ An error occurred: {e}")

if __name__ == "__main__":
    main()
