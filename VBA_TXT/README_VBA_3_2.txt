AUDIO MODEM 3.2 — VBA
======================

Импортировать TXT в стандартные VBA-модули:
modAM32_Config
modAM32_Binary
modAM32_CRC32
modAM32_Frame
modAM32_QAM
modAM32_Link
modAM32_Sound
modAM32_UI
modAM32_Main

Затем запустить Install_Audio_Modem_32.

Архитектура:
Ноутбук TX -> динамик -> телефон RX
Телефон TX -> динамик -> ноутбук RX

Передача полудуплексная:
TRAIN -> CAPS -> DATA -> ACK/NACK -> END.

16 поднесущих:
14 DATA 16-QAM
2 PILOT
48 kHz
80 полезных + 16 cyclic prefix = 96 samples / symbol.

Важно:
Для потокового waveIn на ноутбуке можно использовать modWaveInV3 из 3.0.
В 3.2 основной новый код — совместимый кадр, пилоты и двусторонняя сессия.
