export const mockDoctors = [
  {
    id: '2f9d4b6e-7d5e-4b64-9f55-1f64d8dc1001',
    name: 'Dr. Meera Joshi',
    specialty: 'Internal Medicine',
    experience: '9 years experience',
    fee: 700,
    rating: 4.8,
    reviews: 426,
    nextAvailable: 'Today, 6:00 PM',
    location: 'Pune',
    languages: ['English', 'Hindi', 'Marathi'],
    focusAreas: ['Diabetes care', 'Hypertension', 'Preventive medicine'],
    about:
      'Focused on preventive internal medicine, hypertension management, diabetes reviews, and follow-up care that works well in a remote-first setting.',
    credentials: [
      'MD, Internal Medicine',
      '9 years of teleconsultation experience',
      'Speaks English, Hindi, and Marathi',
      'Available for chronic care and acute review'
    ],
    availability: ['Today 6:00 PM', 'Tomorrow 10:30 AM', 'Friday 11:00 AM', 'Saturday 4:45 PM']
  },
  {
    id: '44b9d995-0a94-4ee8-8bea-1294f3421002',
    name: 'Dr. Aman Kapoor',
    specialty: 'General Physician',
    experience: '12 years experience',
    fee: 600,
    rating: 4.7,
    reviews: 389,
    nextAvailable: 'Today, 7:15 PM',
    location: 'Delhi',
    languages: ['English', 'Hindi', 'Punjabi'],
    focusAreas: ['Fever care', 'Respiratory illness', 'Routine follow-ups'],
    about:
      'Handles common acute conditions, routine health reviews, and quick medical assessments for patients who need timely primary care guidance.',
    credentials: [
      'MBBS, DNB Family Medicine',
      '12 years in primary and urgent care',
      'Experienced in remote triage workflows',
      'Consults in English, Hindi, and Punjabi'
    ],
    availability: ['Today 7:15 PM', 'Tomorrow 9:45 AM', 'Friday 5:30 PM', 'Sunday 10:00 AM']
  },
  {
    id: 'f5f29a3c-5f74-4466-b2c8-d77f31d61003',
    name: 'Dr. Nisha Menon',
    specialty: 'Dermatology',
    experience: '8 years experience',
    fee: 850,
    rating: 4.9,
    reviews: 312,
    nextAvailable: 'Tomorrow, 11:30 AM',
    location: 'Bengaluru',
    languages: ['English', 'Hindi', 'Malayalam'],
    focusAreas: ['Acne care', 'Skin allergy', 'Hair loss'],
    about:
      'Specializes in everyday dermatology concerns and follow-up treatment plans that translate well to a photo-first teleconsultation format.',
    credentials: [
      'MD, Dermatology',
      '8 years of clinical dermatology practice',
      'Experienced with image-based teleconsults',
      'Consults in English, Hindi, and Malayalam'
    ],
    availability: ['Tomorrow 11:30 AM', 'Friday 3:00 PM', 'Saturday 12:15 PM', 'Monday 6:30 PM']
  }
];

export const defaultDoctorId = mockDoctors[0].id;

export function getDoctorById(doctorId) {
  return mockDoctors.find((doctor) => doctor.id === doctorId) ?? null;
}
